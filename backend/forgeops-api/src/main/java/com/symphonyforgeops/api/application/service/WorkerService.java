package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.WorkerHost;
import com.symphonyforgeops.api.interfaces.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerService {

    private final JdbcTemplate jdbcTemplate;

    public WorkerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<WorkerHost> list() {
        return jdbcTemplate.query("""
                        SELECT worker_key, display_name, protocol, host_name, capacity, status,
                               available_capacity, current_runs, last_error
                        FROM worker_hosts
                        ORDER BY id
                        """,
                (rs, rowNum) -> new WorkerHost(
                        rs.getString("worker_key"),
                        rs.getString("display_name"),
                        rs.getString("protocol"),
                        rs.getString("host_name"),
                        rs.getInt("capacity"),
                        rs.getString("status"),
                        rs.getInt("available_capacity"),
                        rs.getInt("current_runs"),
                        rs.getString("last_error")
                ));
    }

    @Transactional
    public WorkerHost register(String workerKey, String displayName, String protocol, String host, int capacity, String status) {
        jdbcTemplate.update("""
                        INSERT INTO worker_hosts (
                            worker_key, display_name, protocol, host_name, capacity, status,
                            available_capacity, current_runs
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                        ON DUPLICATE KEY UPDATE
                            display_name = VALUES(display_name),
                            protocol = VALUES(protocol),
                            host_name = VALUES(host_name),
                            capacity = VALUES(capacity),
                            status = VALUES(status),
                            available_capacity = VALUES(available_capacity)
                        """,
                workerKey,
                displayName,
                protocol,
                host,
                capacity,
                status,
                "online".equals(status) ? capacity : 0);
        return get(workerKey);
    }

    @Transactional
    public WorkerHost heartbeat(String workerKey, String status, int availableCapacity, int currentRuns, String lastError) {
        long workerId = numericId(workerKey);
        jdbcTemplate.update("""
                        INSERT INTO worker_heartbeats (worker_id, status, available_capacity, current_runs, last_error)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                workerId,
                status,
                availableCapacity,
                currentRuns,
                lastError);
        jdbcTemplate.update("""
                        UPDATE worker_hosts
                        SET status = ?, available_capacity = ?, current_runs = ?, last_error = ?, last_heartbeat_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                status,
                availableCapacity,
                currentRuns,
                lastError,
                workerId);
        return get(workerKey);
    }

    WorkerHost acquireAvailable(String agentName) {
        List<WorkerHost> availableWorkers = list().stream()
                .filter(worker -> "online".equals(worker.status()))
                .filter(worker -> worker.availableCapacity() > 0)
                .toList();
        if ("codex".equalsIgnoreCase(agentName == null ? "" : agentName.trim())) {
            return availableWorkers.stream()
                    .filter(this::isCodexWorker)
                    .findFirst()
                    .orElseGet(() -> firstAvailable(availableWorkers));
        }
        return firstAvailable(availableWorkers);
    }

    private WorkerHost firstAvailable(List<WorkerHost> availableWorkers) {
        return availableWorkers.stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "NO_WORKER_CAPACITY", "No online worker has available capacity."));
    }

    private boolean isCodexWorker(WorkerHost worker) {
        String workerKey = worker.workerKey() == null ? "" : worker.workerKey().toLowerCase();
        String displayName = worker.displayName() == null ? "" : worker.displayName().toLowerCase();
        String protocol = worker.protocol() == null ? "" : worker.protocol().toLowerCase();
        return workerKey.contains("codex") || displayName.contains("codex") || protocol.contains("codex");
    }

    long numericId(String workerKey) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM worker_hosts WHERE worker_key = ?",
                (rs, rowNum) -> rs.getLong("id"),
                workerKey);
        if (ids.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", workerKey);
        }
        return ids.get(0);
    }

    private WorkerHost get(String workerKey) {
        return list().stream()
                .filter(worker -> worker.workerKey().equals(workerKey))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", workerKey));
    }
}
