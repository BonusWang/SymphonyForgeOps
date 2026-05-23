package com.symphonyforgeops.api.interfaces.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControlPlaneApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void projectsCanBeCreatedListedUpdatedAndArchived() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectKey": "wikiforge",
                                  "name": "WikiForge",
                                  "repoUrl": "https://github.com/BonusWang/WikiForge",
                                  "localPath": "E:/github/WikiForge",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "E:/ForgeOps/workspaces/wikiforge",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["mvn -B test", "npm run build"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("wikiforge")))
                .andExpect(jsonPath("$.data.commandTemplates", hasSize(2)));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].workspaceRoot", is("E:/ForgeOps/workspaces/wikiforge")));

        mockMvc.perform(put("/api/v1/projects/wikiforge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "WikiForge Knowledge Product",
                                  "repoUrl": "https://github.com/BonusWang/WikiForge",
                                  "localPath": "E:/github/WikiForge",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "E:/ForgeOps/workspaces/wikiforge",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["mvn -B test"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("WikiForge Knowledge Product")))
                .andExpect(jsonPath("$.data.commandTemplates", hasSize(1)));

        mockMvc.perform(post("/api/v1/projects/wikiforge/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("archived")));
    }

    @Test
    void workOrdersCanBeCreatedListedAndTransitioned() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectKey": "symphony-forgeops",
                                  "name": "SymphonyForgeOps",
                                  "repoUrl": "https://github.com/BonusWang/SymphonyForgeOps",
                                  "localPath": "E:/github/SymphonyForgeOps",
                                  "defaultBranch": "main",
                                  "workflowPath": "WORKFLOW.md",
                                  "workspaceRoot": "E:/ForgeOps/workspaces/symphony-forgeops",
                                  "stack": "Java 17 / Spring Boot / Vue 3 / MySQL",
                                  "status": "active",
                                  "commandTemplates": ["mvn -B test"]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workOrderKey": "wo-persist-001",
                                  "projectId": "symphony-forgeops",
                                  "title": "Persist control plane",
                                  "status": "ready",
                                  "implementationAgent": "Codex",
                                  "testCommand": "mvn -B test",
                                  "reviewCommand": "manual review",
                                  "humanApprovalRequired": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is("wo-persist-001")))
                .andExpect(jsonPath("$.data.status", is("ready")));

        mockMvc.perform(get("/api/v1/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].projectId", is("symphony-forgeops")));

        mockMvc.perform(post("/api/v1/work-orders/wo-persist-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "running"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("running")));

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectCount", is(1)))
                .andExpect(jsonPath("$.data.openWorkOrderCount", is(1)));
    }
}
