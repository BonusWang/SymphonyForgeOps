package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.application.dto.WorkOrderRequest;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class WorkOrderService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;

    public WorkOrderService(JdbcTemplate jdbcTemplate, ProjectService projectService) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
    }

    public List<WorkOrder> list() {
        return jdbcTemplate.query("""
                        SELECT wo.work_order_key, mp.project_key, wo.title, wo.status, wo.implementation_agent,
                               wo.test_command, wo.review_command, wo.human_approval_required
                        FROM work_orders wo
                        JOIN managed_projects mp ON mp.id = wo.project_id
                        ORDER BY wo.id
                        """,
                (rs, rowNum) -> new WorkOrder(
                        rs.getString("work_order_key"),
                        rs.getString("project_key"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("implementation_agent"),
                        rs.getString("test_command"),
                        rs.getString("review_command"),
                        rs.getBoolean("human_approval_required")
                ));
    }

    public WorkOrder get(String workOrderKey) {
        return list().stream()
                .filter(workOrder -> workOrder.id().equals(workOrderKey))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Work order not found: " + workOrderKey));
    }

    long numericId(String workOrderKey) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM work_orders WHERE work_order_key = ?",
                (rs, rowNum) -> rs.getLong("id"),
                workOrderKey);
        if (ids.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Work order not found: " + workOrderKey);
        }
        return ids.get(0);
    }

    @Transactional
    public WorkOrder create(WorkOrderRequest request) {
        long projectId = projectService.numericId(request.projectId());
        jdbcTemplate.update("""
                        INSERT INTO work_orders (
                            work_order_key, project_id, title, status, implementation_agent,
                            test_command, review_command, human_approval_required
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                request.workOrderKey(),
                projectId,
                request.title(),
                defaultString(request.status(), "ready"),
                request.implementationAgent(),
                request.testCommand(),
                request.reviewCommand(),
                request.humanApprovalRequired());
        return get(request.workOrderKey());
    }

    @Transactional
    public WorkOrder updateStatus(String workOrderKey, String status) {
        jdbcTemplate.update("UPDATE work_orders SET status = ? WHERE work_order_key = ?", status, workOrderKey);
        return get(workOrderKey);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
