package com.symphonyforgeops.api.interfaces.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM artifacts");
        jdbcTemplate.update("DELETE FROM human_decisions");
        jdbcTemplate.update("DELETE FROM review_findings");
        jdbcTemplate.update("DELETE FROM github_links");
        jdbcTemplate.update("DELETE FROM agent_adapter_configs");
        jdbcTemplate.update("DELETE FROM worker_assignments");
        jdbcTemplate.update("DELETE FROM worker_heartbeats");
        jdbcTemplate.update("DELETE FROM worker_hosts");
        jdbcTemplate.update("DELETE FROM command_runs");
        jdbcTemplate.update("DELETE FROM run_events");
        jdbcTemplate.update("DELETE FROM retry_queue");
        jdbcTemplate.update("DELETE FROM run_attempts");
        jdbcTemplate.update("DELETE FROM isolated_workspaces");
        jdbcTemplate.update("DELETE FROM workflow_contracts");
        jdbcTemplate.update("DELETE FROM review_items");
        jdbcTemplate.update("DELETE FROM agent_runs");
        jdbcTemplate.update("DELETE FROM work_orders");
        jdbcTemplate.update("DELETE FROM command_templates");
        jdbcTemplate.update("DELETE FROM managed_projects");
    }

    @Test
    void workflowReloadPersistsTypedContract() throws Exception {
        Path projectPath = tempDir.resolve("workflow-project");
        Path workspaceRoot = tempDir.resolve("workspaces");
        Files.createDirectories(projectPath);
        Files.writeString(projectPath.resolve("WORKFLOW.md"), """
                # ForgeOps Workflow
                tracker_kind: github
                active_states: ready,running,review
                terminal_states: done,cancelled
                polling_interval_ms: 15000
                max_concurrent_agents: 2
                max_turns: 8
                workspace_root: %s
                prompt: Execute the work order and record events.
                """.formatted(workspaceRoot.toString().replace("\\", "/")));

        createProject("workflow-project", projectPath, workspaceRoot);

        mockMvc.perform(post("/api/v1/workflows/reload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "workflow-project"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId", is("workflow-project")))
                .andExpect(jsonPath("$.data.trackerKind", is("github")))
                .andExpect(jsonPath("$.data.maxConcurrentAgents", is(2)))
                .andExpect(jsonPath("$.data.lastKnownGood", is(true)));

        mockMvc.perform(get("/api/v1/workflows/workflow-project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeStates[1]", is("running")))
                .andExpect(jsonPath("$.data.workspaceRoot", is(workspaceRoot.toString().replace("\\", "/"))));
    }

    @Test
    void commandRunWorkspaceSafetyAndWorkerDispatchAreRecorded() throws Exception {
        Path projectPath = tempDir.resolve("runtime-project");
        Path workspaceRoot = tempDir.resolve("runtime-workspaces");
        Files.createDirectories(projectPath);
        Files.createDirectories(workspaceRoot);
        createProject("runtime-project", projectPath, workspaceRoot);
        createWorkOrder();

        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-runtime-001",
                                  "workspaceKey": "../escape",
                                  "relativePath": "../escape"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("WORKSPACE_PATH_OUTSIDE_ROOT")));

        mockMvc.perform(post("/api/v1/commands/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": "runtime-project",
                                  "workOrderId": "wo-runtime-001",
                                  "command": "rm -rf target",
                                  "destructive": true,
                                  "approved": false
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DESTRUCTIVE_COMMAND_REQUIRES_APPROVAL")));

        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workerKey": "local-ssh",
                                  "displayName": "Local SSH Worker",
                                  "protocol": "ssh",
                                  "host": "localhost",
                                  "capacity": 1,
                                  "status": "online"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("online")));

        mockMvc.perform(post("/api/v1/workers/local-ssh/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "online",
                                  "availableCapacity": 1,
                                  "currentRuns": 0,
                                  "lastError": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableCapacity", is(1)));

        mockMvc.perform(post("/api/v1/orchestrator/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-runtime-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workerKey", is("local-ssh")))
                .andExpect(jsonPath("$.data.status", is("succeeded")))
                .andExpect(jsonPath("$.data.exitCode", is(0)))
                .andExpect(jsonPath("$.data.stdout", containsString("forgeops-v1")));

        mockMvc.perform(get("/api/v1/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].currentRuns", is(0)));

        mockMvc.perform(get("/api/v1/orchestrator/events/wo-runtime-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[2].eventType", is("run_succeeded")));
    }

    private void createProject(String projectKey, Path projectPath, Path workspaceRoot) throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectKey": "%s",
                                  "name": "%s",
                                  "repoUrl": "https://github.com/BonusWang/%s",
                                  "localPath": "%s",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "%s",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["echo forgeops-v1"]
                                }
                                """.formatted(
                                projectKey,
                                projectKey,
                                projectKey,
                                projectPath.toString().replace("\\", "/"),
                                workspaceRoot.toString().replace("\\", "/")
                        )))
                .andExpect(status().isOk());
    }

    private void createWorkOrder() throws Exception {
        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderKey": "wo-runtime-001",
                                  "projectId": "runtime-project",
                                  "title": "Run v1 local remote worker acceptance",
                                  "status": "ready",
                                  "implementationAgent": "Manual",
                                  "testCommand": "echo forgeops-v1",
                                  "reviewCommand": "manual review",
                                  "humanApprovalRequired": true
                                }
                                """))
                .andExpect(status().isOk());
    }
}
