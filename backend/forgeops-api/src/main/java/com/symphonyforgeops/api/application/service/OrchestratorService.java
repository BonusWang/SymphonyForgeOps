package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.DispatchResult;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import com.symphonyforgeops.api.domain.model.RunEvent;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import com.symphonyforgeops.api.domain.model.WorkerHost;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class OrchestratorService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final WorkOrderService workOrderService;
    private final WorkerService workerService;
    private final Environment environment;

    public OrchestratorService(JdbcTemplate jdbcTemplate,
                               ProjectService projectService,
                               WorkOrderService workOrderService,
                               WorkerService workerService,
                               Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.workOrderService = workOrderService;
        this.workerService = workerService;
        this.environment = environment;
    }

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
        ShellResult shellResult;
        if (isCodex(workOrder)) {
            ManagedProject project = projectService.get(workOrder.projectId());
            event(agentRunId, "codex_started", "Codex agent started for " + workOrderId);
            shellResult = runCodex(project, workOrder);
            event(agentRunId, "codex_completed", "Codex agent exited with code " + shellResult.exitCode());
        } else {
            event(agentRunId, "command_started", workOrder.testCommand());
            shellResult = runShell(workOrder.testCommand());
        }
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

    private ShellResult runCodex(ManagedProject project, WorkOrder workOrder) {
        int timeoutSeconds = environment.getProperty("forgeops.codex.timeout-seconds", Integer.class, 300);
        String commandTemplate = environment.getProperty("forgeops.codex.command-template", "");
        if (commandTemplate != null && !commandTemplate.isBlank()) {
            return runShell(applyTemplate(commandTemplate, project, workOrder), timeoutSeconds);
        }

        try {
            Path promptFile = Files.createTempFile("forgeops-codex-prompt-", ".md");
            Path outputFile = Files.createTempFile("forgeops-codex-result-", ".md");
            Files.writeString(promptFile, codexPrompt(project, workOrder), StandardCharsets.UTF_8);

            ShellResult shellResult = runCodexCli(project, promptFile, outputFile, timeoutSeconds);
            String finalMessage = Files.exists(outputFile)
                    ? Files.readString(outputFile, StandardCharsets.UTF_8)
                    : "";
            String stdout = finalMessage.isBlank()
                    ? shellResult.stdout()
                    : shellResult.stdout() + "\n[final]\n" + finalMessage;
            return new ShellResult(shellResult.exitCode(), stdout, shellResult.stderr());
        } catch (IOException exception) {
            return new ShellResult(127, "", exception.getMessage());
        }
    }

    private ShellResult runCodexCli(ManagedProject project, Path promptFile, Path outputFile, int timeoutSeconds) throws IOException {
        ProcessBuilder processBuilder;
        if (isWindows()) {
            Path scriptFile = Files.createTempFile("forgeops-codex-run-", ".ps1");
            Files.writeString(scriptFile, """
                    param(
                        [string]$PromptFile,
                        [string]$ProjectPath,
                        [string]$OutputFile
                    )

                    Get-Content -Raw -LiteralPath $PromptFile | codex -C $ProjectPath --sandbox workspace-write --ask-for-approval never exec --output-last-message $OutputFile -
                    exit $LASTEXITCODE
                    """, StandardCharsets.UTF_8);
            processBuilder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    scriptFile.toString(),
                    promptFile.toString(),
                    project.localPath(),
                    outputFile.toString()
            );
        } else {
            processBuilder = new ProcessBuilder(
                    "sh",
                    "-lc",
                    "cat \"$1\" | codex -C \"$2\" --sandbox workspace-write --ask-for-approval never exec --output-last-message \"$3\" -",
                    "sh",
                    promptFile.toString(),
                    project.localPath(),
                    outputFile.toString()
            );
        }
        return runProcess(processBuilder, timeoutSeconds);
    }

    private String codexPrompt(ManagedProject project, WorkOrder workOrder) {
        return """
                You are a Codex verification agent dispatched by SymphonyForgeOps.

                Project:
                - id: %s
                - name: %s
                - local path: %s

                Work order:
                - id: %s
                - title: %s
                - verification command: %s

                Run or inspect the verification command from the project root. Do not change source code unless the verification task explicitly requires it.
                Return the final result with: status, command/output summary, and any follow-up needed.
                """.formatted(
                project.id(),
                project.name(),
                project.localPath(),
                workOrder.id(),
                workOrder.title(),
                workOrder.testCommand()
        );
    }

    private String applyTemplate(String template, ManagedProject project, WorkOrder workOrder) {
        return template
                .replace("{projectId}", nullToBlank(project.id()))
                .replace("{projectPath}", nullToBlank(project.localPath()))
                .replace("{workOrderId}", nullToBlank(workOrder.id()))
                .replace("{title}", nullToBlank(workOrder.title()))
                .replace("{testCommand}", nullToBlank(workOrder.testCommand()));
    }

    private boolean isCodex(WorkOrder workOrder) {
        return "codex".equalsIgnoreCase(nullToBlank(workOrder.implementationAgent()).trim());
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
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
        return runShell(command, 30);
    }

    private ShellResult runShell(String command, int timeoutSeconds) {
        ProcessBuilder processBuilder;
        if (isWindows()) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            processBuilder = new ProcessBuilder("sh", "-lc", command);
        }
        return runProcess(processBuilder, timeoutSeconds);
    }

    private ShellResult runProcess(ProcessBuilder processBuilder, int timeoutSeconds) {
        try {
            Process process = processBuilder.start();
            CompletableFuture<String> stdout = readAsync(process.getInputStream());
            CompletableFuture<String> stderr = readAsync(process.getErrorStream());
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new ShellResult(124, stdout.getNow(""), "Command timed out\n" + stderr.getNow(""));
            }
            return new ShellResult(
                    process.exitValue(),
                    stdout.join(),
                    stderr.join()
            );
        } catch (IOException exception) {
            return new ShellResult(127, "", exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new ShellResult(130, "", exception.getMessage());
        }
    }

    private CompletableFuture<String> readAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                return exception.getMessage();
            }
        });
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private record ShellResult(int exitCode, String stdout, String stderr) {
    }
}
