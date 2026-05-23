package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.IsolatedWorkspace;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import com.symphonyforgeops.api.interfaces.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Service
public class WorkspaceService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final WorkOrderService workOrderService;

    public WorkspaceService(JdbcTemplate jdbcTemplate, ProjectService projectService, WorkOrderService workOrderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.workOrderService = workOrderService;
    }

    @Transactional
    public IsolatedWorkspace create(String workOrderId, String workspaceKey, String relativePath) {
        WorkOrder workOrder = workOrderService.get(workOrderId);
        ManagedProject project = projectService.get(workOrder.projectId());
        String cleanKey = workspaceKey.replaceAll("[^A-Za-z0-9._-]", "-");
        Path root = Path.of(project.workspaceRoot()).toAbsolutePath().normalize();
        Path workspace = root.resolve(relativePath == null || relativePath.isBlank() ? cleanKey : relativePath).normalize();
        if (!workspace.startsWith(root)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WORKSPACE_PATH_OUTSIDE_ROOT", "Workspace must stay under project workspace root.");
        }
        jdbcTemplate.update("""
                        INSERT INTO isolated_workspaces (work_order_id, workspace_key, workspace_path, status, created_now)
                        VALUES (?, ?, ?, 'created', TRUE)
                        """,
                workOrderService.numericId(workOrderId),
                cleanKey,
                workspace.toString());
        return new IsolatedWorkspace(workOrderId, cleanKey, workspace.toString(), "created");
    }
}
