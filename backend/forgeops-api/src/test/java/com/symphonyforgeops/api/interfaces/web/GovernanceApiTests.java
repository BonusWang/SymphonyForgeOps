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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GovernanceApiTests {

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
    void agentAdaptersGithubLinksReviewsAndHumanDecisionsArePersisted() throws Exception {
        Path projectPath = tempDir.resolve("governance-project");
        Path workspaceRoot = tempDir.resolve("governance-workspaces");
        Files.createDirectories(projectPath);
        createProject(projectPath, workspaceRoot);
        createWorkOrder();

        mockMvc.perform(post("/api/v1/agent-runs/adapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agentName": "Codex",
                                  "adapterType": "external",
                                  "enabled": true,
                                  "smokeStatus": "skipped",
                                  "smokeReason": "External CLI smoke is optional in local v1 tests."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentName", is("Codex")))
                .andExpect(jsonPath("$.data.smokeStatus", is("skipped")));

        mockMvc.perform(post("/api/v1/github-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-governance-001",
                                  "issueUrl": "https://github.com/BonusWang/SymphonyForgeOps/issues/1",
                                  "prUrl": "https://github.com/BonusWang/SymphonyForgeOps/pull/2",
                                  "checkStatus": "pending"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkStatus", is("pending")));

        mockMvc.perform(post("/api/v1/reviews/findings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-governance-001",
                                  "source": "manual",
                                  "severity": "medium",
                                  "summary": "Review gate recorded.",
                                  "status": "open"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.severity", is("medium")));

        mockMvc.perform(post("/api/v1/reviews/human-decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderId": "wo-governance-001",
                                  "decision": "approved",
                                  "reason": "Risk accepted for v1.",
                                  "decider": "Domain1127"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decision", is("approved")));

        mockMvc.perform(get("/api/v1/github-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.findings", hasSize(1)))
                .andExpect(jsonPath("$.data.humanDecisions", hasSize(1)));
    }

    private void createProject(Path projectPath, Path workspaceRoot) throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectKey": "governance-project",
                                  "name": "Governance Project",
                                  "repoUrl": "https://github.com/BonusWang/SymphonyForgeOps",
                                  "localPath": "%s",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "%s",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["echo governance"]
                                }
                                """.formatted(
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
                                  "workOrderKey": "wo-governance-001",
                                  "projectId": "governance-project",
                                  "title": "Govern v1 release",
                                  "status": "ready",
                                  "implementationAgent": "Codex",
                                  "testCommand": "echo governance",
                                  "reviewCommand": "manual review",
                                  "humanApprovalRequired": true
                                }
                                """))
                .andExpect(status().isOk());
    }
}
