package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.application.dto.ProjectRequest;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProjectService {

    private final JdbcTemplate jdbcTemplate;

    public ProjectService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ManagedProject> list() {
        return jdbcTemplate.query("""
                        SELECT project_key, name, repo_url, local_path, default_branch, workflow_path,
                               workspace_root, stack_summary, status
                        FROM managed_projects
                        ORDER BY id
                        """,
                (rs, rowNum) -> new ManagedProject(
                        rs.getString("project_key"),
                        rs.getString("name"),
                        rs.getString("repo_url"),
                        rs.getString("local_path"),
                        rs.getString("default_branch"),
                        rs.getString("workflow_path"),
                        rs.getString("workspace_root"),
                        rs.getString("stack_summary"),
                        rs.getString("status"),
                        commandTemplates(rs.getString("project_key"))
                ));
    }

    public ManagedProject get(String projectKey) {
        return list().stream()
                .filter(project -> project.id().equals(projectKey))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Project not found: " + projectKey));
    }

    @Transactional
    public ManagedProject create(ProjectRequest request) {
        jdbcTemplate.update("""
                        INSERT INTO managed_projects (
                            project_key, name, repo_url, local_path, default_branch, workflow_path,
                            workspace_root, stack_summary, status
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                request.projectKey(),
                request.name(),
                request.repoUrl(),
                request.localPath(),
                defaultString(request.defaultBranch(), "main"),
                defaultString(request.workflowPath(), "WORKFLOW.md"),
                request.workspaceRoot(),
                request.stack(),
                defaultString(request.status(), "active"));
        replaceCommands(request.projectKey(), request.commandTemplates());
        return get(request.projectKey());
    }

    @Transactional
    public ManagedProject update(String projectKey, ProjectRequest request) {
        jdbcTemplate.update("""
                        UPDATE managed_projects
                        SET name = ?, repo_url = ?, local_path = ?, default_branch = ?, workflow_path = ?,
                            workspace_root = ?, stack_summary = ?, status = ?
                        WHERE project_key = ?
                        """,
                request.name(),
                request.repoUrl(),
                request.localPath(),
                defaultString(request.defaultBranch(), "main"),
                defaultString(request.workflowPath(), "WORKFLOW.md"),
                request.workspaceRoot(),
                request.stack(),
                defaultString(request.status(), "active"),
                projectKey);
        replaceCommands(projectKey, request.commandTemplates());
        return get(projectKey);
    }

    @Transactional
    public ManagedProject archive(String projectKey) {
        jdbcTemplate.update("UPDATE managed_projects SET status = 'archived' WHERE project_key = ?", projectKey);
        return get(projectKey);
    }

    long numericId(String projectKey) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM managed_projects WHERE project_key = ?",
                (rs, rowNum) -> rs.getLong("id"),
                projectKey);
        if (ids.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Project not found: " + projectKey);
        }
        return ids.get(0);
    }

    private void replaceCommands(String projectKey, List<String> commands) {
        long projectId = numericId(projectKey);
        jdbcTemplate.update("DELETE FROM command_templates WHERE project_id = ?", projectId);
        if (commands == null) {
            return;
        }
        for (int i = 0; i < commands.size(); i++) {
            jdbcTemplate.update("""
                            INSERT INTO command_templates (
                                project_id, command_key, command_type, command_text, working_directory, is_destructive
                            )
                            VALUES (?, ?, 'test', ?, NULL, FALSE)
                            """,
                    projectId,
                    "cmd-" + (i + 1),
                    commands.get(i));
        }
    }

    private List<String> commandTemplates(String projectKey) {
        return jdbcTemplate.query("""
                        SELECT ct.command_text
                        FROM command_templates ct
                        JOIN managed_projects mp ON mp.id = ct.project_id
                        WHERE mp.project_key = ?
                        ORDER BY ct.id
                        """,
                (rs, rowNum) -> rs.getString("command_text"),
                projectKey);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
