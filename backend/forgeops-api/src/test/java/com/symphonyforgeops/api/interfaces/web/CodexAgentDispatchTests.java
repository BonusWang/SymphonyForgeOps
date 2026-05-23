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

@SpringBootTest(properties = {
        "forgeops.codex.command-template=echo codex-agent-verified {workOrderId} {testCommand}"
})
@AutoConfigureMockMvc
class CodexAgentDispatchTests {

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
    void wikiforgeVerificationWorkOrderIsHandledByCodexAgent() throws Exception {
        Path projectPath = tempDir.resolve("WikiForge");
        Path workspaceRoot = tempDir.resolve("forgeops-workspaces").resolve("wikiforge");
        Files.createDirectories(projectPath);
        Files.createDirectories(workspaceRoot);
        Files.writeString(projectPath.resolve("WORKFLOW.md"), "# WikiForge workflow\n");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectKey": "wikiforge",
                                  "name": "WikiForge",
                                  "repoUrl": "https://github.com/BonusWang/WikiForge",
                                  "localPath": "%s",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "%s",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["echo wikiforge-test-ok"]
                                }
                                """.formatted(
                                projectPath.toString().replace("\\", "/"),
                                workspaceRoot.toString().replace("\\", "/")
                        )))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderKey": "wo-wikiforge-codex-verify",
                                  "projectId": "wikiforge",
                                  "title": "Codex verifies WikiForge test command",
                                  "status": "ready",
                                  "implementationAgent": "Codex",
                                  "testCommand": "echo wikiforge-test-ok",
                                  "reviewCommand": "manual review",
                                  "humanApprovalRequired": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workerKey": "manual-local",
                                  "displayName": "Manual Local Worker",
                                  "protocol": "local",
                                  "host": "localhost",
                                  "capacity": 1,
                                  "status": "online"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/workers/manual-local/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "online",
                                  "availableCapacity": 1,
                                  "currentRuns": 0,
                                  "lastError": null
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workerKey": "codex-local",
                                  "displayName": "Local Codex Worker",
                                  "protocol": "local",
                                  "host": "localhost",
                                  "capacity": 1,
                                  "status": "online"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/workers/codex-local/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "online",
                                  "availableCapacity": 1,
                                  "currentRuns": 0,
                                  "lastError": null
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orchestrator/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-wikiforge-codex-verify"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workerKey", is("codex-local")))
                .andExpect(jsonPath("$.data.status", is("succeeded")))
                .andExpect(jsonPath("$.data.stdout", containsString("codex-agent-verified")))
                .andExpect(jsonPath("$.data.stdout", containsString("wo-wikiforge-codex-verify")));

        mockMvc.perform(get("/api/v1/orchestrator/events/wo-wikiforge-codex-verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[1].eventType", is("codex_started")))
                .andExpect(jsonPath("$.data[2].eventType", is("codex_completed")))
                .andExpect(jsonPath("$.data[3].eventType", is("run_succeeded")));

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projects[0].id", is("wikiforge")))
                .andExpect(jsonPath("$.data.workOrders[0].implementationAgent", is("Codex")))
                .andExpect(jsonPath("$.data.agentRuns[0].agentName", is("Codex")))
                .andExpect(jsonPath("$.data.agentRuns[0].summary", containsString("codex-agent-verified")));
    }
}
