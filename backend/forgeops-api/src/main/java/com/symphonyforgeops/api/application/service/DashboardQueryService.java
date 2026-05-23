package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.AgentRun;
import com.symphonyforgeops.api.domain.model.DashboardSnapshot;
import com.symphonyforgeops.api.domain.model.ReviewItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final WorkOrderService workOrderService;

    public DashboardQueryService(
            JdbcTemplate jdbcTemplate,
            ProjectService projectService,
            WorkOrderService workOrderService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.workOrderService = workOrderService;
    }

    public DashboardSnapshot snapshot() {
        List<AgentRun> agentRuns = agentRuns();
        List<ReviewItem> reviewItems = reviewItems();
        return new DashboardSnapshot(
                count("SELECT COUNT(*) FROM managed_projects WHERE status <> 'archived'"),
                count("SELECT COUNT(*) FROM work_orders WHERE status NOT IN ('done', 'cancelled', 'archived')"),
                count("SELECT COUNT(*) FROM agent_runs WHERE status = 'running'"),
                count("SELECT COUNT(*) FROM review_items WHERE status = 'pending'"),
                count("SELECT COUNT(*) FROM retry_queue WHERE status = 'scheduled'"),
                count("SELECT COUNT(*) FROM workflow_contracts WHERE status = 'active'"),
                "watching_work_orders",
                workspaceRoot(),
                projectService.list(),
                workOrderService.list(),
                agentRuns,
                reviewItems
        );
    }

    private int count(String sql) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    private String workspaceRoot() {
        return jdbcTemplate.query("""
                        SELECT workspace_root
                        FROM managed_projects
                        WHERE workspace_root IS NOT NULL AND workspace_root <> ''
                        ORDER BY id
                        LIMIT 1
                        """,
                (rs, rowNum) -> rs.getString("workspace_root"))
                .stream()
                .findFirst()
                .orElse("");
    }

    private List<AgentRun> agentRuns() {
        return jdbcTemplate.query("""
                        SELECT ar.run_key, wo.work_order_key, ar.agent_name, ar.status, ar.summary,
                               DATE_FORMAT(ar.started_at, '%Y-%m-%dT%H:%i:%s+08:00') AS started_at
                        FROM agent_runs ar
                        JOIN work_orders wo ON wo.id = ar.work_order_id
                        ORDER BY ar.id
                        """,
                (rs, rowNum) -> new AgentRun(
                        rs.getString("run_key"),
                        rs.getString("work_order_key"),
                        rs.getString("agent_name"),
                        rs.getString("status"),
                        rs.getString("summary"),
                        rs.getString("started_at")
                ));
    }

    private List<ReviewItem> reviewItems() {
        return jdbcTemplate.query("""
                        SELECT ri.review_key, wo.work_order_key, ri.review_type, ri.status, ri.risk_level, ri.summary
                        FROM review_items ri
                        JOIN work_orders wo ON wo.id = ri.work_order_id
                        ORDER BY ri.id
                        """,
                (rs, rowNum) -> new ReviewItem(
                        rs.getString("review_key"),
                        rs.getString("work_order_key"),
                        rs.getString("review_type"),
                        rs.getString("status"),
                        rs.getString("risk_level"),
                        rs.getString("summary")
                ));
    }
}
