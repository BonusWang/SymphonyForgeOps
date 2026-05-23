package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.DispatchResult;
import com.symphonyforgeops.api.domain.model.RunEvent;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import com.symphonyforgeops.api.domain.model.WorkerHost;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrchestratorService {

    private final JdbcTemplate jdbcTemplate;
    private final WorkOrderService workOrderService;
    private final WorkerService workerService;

    public OrchestratorService(JdbcTemplate jdbcTemplate, WorkOrderService workOrderService, WorkerService workerService) {
        this.jdbcTemplate = jdbcTemplate;
        this.workOrderService = workOrderService;
        this.workerService = workerService;
    }

    @Transactional
    public DispatchResult dispatch(String workOrderId) {
        WorkOrder workOrder = workOrderService.get(workOrderId);
        WorkerHost worker = workerService.acquireAvailable();
        long workOrderNumericId = workOrderService.numericId(workOrderId);
        long agentRunId = createAgentRun(workOrderNumericId, workOrder.implementationAgent());
        event(agentRunId, "claimed", "Claimed work order " + workOrderId);
        jdbcTemplate.update("""
                        INSERT INTO worker_assignments (worker_id, work_order_id, agent_run_id, status)
                        VALUES (?, ?, ?, 'running')
                        """,
                workerService.numericId(worker.workerKey()),
                workOrderNumericId,
                agentRunId);
        Long assignmentId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        event(agentRunId, "command_started", workOrder.testCommand());
        ShellResult shellResult = runShell(workOrder.testCommand());
        String status = shellResult.exitCode() == 0 ? "succeeded" : "failed";
        jdbcTemplate.update("""
                        UPDATE worker_assignments
                        SET status = ?, exit_code = ?, stdout_text = ?, stderr_text = ?, finished_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                status,
                shellResult.exitCode(),
                shellResult.stdout(),
                shellResult.stderr(),
                assignmentId);
        jdbcTemplate.update("UPDATE agent_runs SET status = ?, summary = ?, finished_at = CURRENT_TIMESTAMP WHERE id = ?",
                status,
                shellResult.stdout(),
                agentRunId);
        workOrderService.updateStatus(workOrderId, status);
        event(agentRunId, "run_" + status, "Worker " + worker.workerKey() + " finished with exit code " + shellResult.exitCode());
        return new DispatchResult(workOrderId, worker.workerKey(), status, shellResult.exitCode(), shellResult.stdout(), shellResult.stderr());
    }

    public List<RunEvent> events(String workOrderId) {
        return jdbcTemplate.query("""
                        SELECT wo.work_order_key, re.event_type, re.message
                        FROM run_events re
                        JOIN agent_runs ar ON ar.id = re.agent_run_id
                        JOIN work_orders wo ON wo.id = ar.work_order_id
                        WHERE wo.work_order_key = ?
                        ORDER BY re.id
                        """,
                (rs, rowNum) -> new RunEvent(
                        rs.getString("work_order_key"),
                        rs.getString("event_type"),
                        rs.getString("message")
                ),
                workOrderId);
    }

    private long createAgentRun(long workOrderId, String agentName) {
        jdbcTemplate.update("""
                        INSERT INTO agent_runs (run_key, work_order_id, agent_name, status, summary, started_at)
                        VALUES (?, ?, ?, 'running', '', CURRENT_TIMESTAMP)
                        """,
                "run-" + workOrderId + "-" + System.currentTimeMillis(),
                workOrderId,
                agentName == null || agentName.isBlank() ? "Manual" : agentName);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0 : id;
    }

    private void event(long agentRunId, String eventType, String message) {
        jdbcTemplate.update("""
                        INSERT INTO run_events (agent_run_id, event_type, message, payload)
                        VALUES (?, ?, ?, JSON_OBJECT())
                        """,
                agentRunId,
                eventType,
                message);
    }

    private ShellResult runShell(String command) {
        ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            processBuilder = new ProcessBuilder("sh", "-lc", command);
        }
        try {
            Process process = processBuilder.start();
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new ShellResult(124, "", "Command timed out");
            }
            return new ShellResult(
                    process.exitValue(),
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8),
                    new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            return new ShellResult(127, "", exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new ShellResult(130, "", exception.getMessage());
        }
    }

    private record ShellResult(int exitCode, String stdout, String stderr) {
    }
}
