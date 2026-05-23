package com.symphonyforgeops.api.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import com.symphonyforgeops.api.domain.model.WorkflowContract;
import com.symphonyforgeops.api.interfaces.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    public WorkflowService(JdbcTemplate jdbcTemplate, ProjectService projectService, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WorkflowContract reload(String projectId) {
        ManagedProject project = projectService.get(projectId);
        Path workflowFile = Path.of(project.localPath()).resolve(project.workflowPath()).normalize();
        if (!Files.exists(workflowFile)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WORKFLOW_NOT_FOUND", "Workflow file not found: " + workflowFile);
        }
        Map<String, String> values;
        try {
            values = Files.readAllLines(workflowFile).stream()
                    .filter(line -> line.contains(":"))
                    .map(line -> line.split(":", 2))
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim(), (left, right) -> right));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WORKFLOW_READ_FAILED", exception.getMessage());
        }

        WorkflowContract contract = new WorkflowContract(
                project.id(),
                project.workflowPath(),
                value(values, "tracker_kind", "manual"),
                csv(values.getOrDefault("active_states", "ready,running,review")),
                csv(values.getOrDefault("terminal_states", "done,cancelled")),
                integer(values.get("polling_interval_ms"), 30000),
                integer(values.get("max_concurrent_agents"), 1),
                integer(values.get("max_turns"), 20),
                value(values, "workspace_root", project.workspaceRoot()),
                value(values, "prompt", ""),
                "active",
                true
        );
        jdbcTemplate.update("""
                        INSERT INTO workflow_contracts (
                            project_id, workflow_path, tracker_kind, active_states, terminal_states,
                            polling_interval_ms, max_concurrent_agents, max_turns, workspace_root,
                            prompt_body, status
                        )
                        VALUES (?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            tracker_kind = VALUES(tracker_kind),
                            active_states = VALUES(active_states),
                            terminal_states = VALUES(terminal_states),
                            polling_interval_ms = VALUES(polling_interval_ms),
                            max_concurrent_agents = VALUES(max_concurrent_agents),
                            max_turns = VALUES(max_turns),
                            workspace_root = VALUES(workspace_root),
                            prompt_body = VALUES(prompt_body),
                            status = VALUES(status)
                        """,
                projectService.numericId(project.id()),
                contract.workflowPath(),
                contract.trackerKind(),
                json(contract.activeStates()),
                json(contract.terminalStates()),
                contract.pollingIntervalMs(),
                contract.maxConcurrentAgents(),
                contract.maxTurns(),
                contract.workspaceRoot(),
                contract.promptBody(),
                contract.status());
        return contract;
    }

    public WorkflowContract get(String projectId) {
        return jdbcTemplate.query("""
                        SELECT mp.project_key, wc.workflow_path, wc.tracker_kind, wc.active_states, wc.terminal_states,
                               wc.polling_interval_ms, wc.max_concurrent_agents, wc.max_turns, wc.workspace_root,
                               wc.prompt_body, wc.status
                        FROM workflow_contracts wc
                        JOIN managed_projects mp ON mp.id = wc.project_id
                        WHERE mp.project_key = ?
                        ORDER BY wc.updated_at DESC
                        LIMIT 1
                        """,
                (rs, rowNum) -> new WorkflowContract(
                        rs.getString("project_key"),
                        rs.getString("workflow_path"),
                        rs.getString("tracker_kind"),
                        list(rs.getString("active_states")),
                        list(rs.getString("terminal_states")),
                        rs.getInt("polling_interval_ms"),
                        rs.getInt("max_concurrent_agents"),
                        rs.getInt("max_turns"),
                        rs.getString("workspace_root"),
                        rs.getString("prompt_body"),
                        rs.getString("status"),
                        true
                ),
                projectId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WORKFLOW_CONTRACT_NOT_FOUND", projectId));
    }

    private String value(Map<String, String> values, String key, String fallback) {
        String value = values.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private int integer(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    private List<String> csv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String json(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private List<String> list(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
