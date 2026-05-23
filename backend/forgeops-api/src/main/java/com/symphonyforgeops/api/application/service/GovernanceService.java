package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.AgentAdapterConfig;
import com.symphonyforgeops.api.domain.model.GithubLink;
import com.symphonyforgeops.api.domain.model.HumanDecision;
import com.symphonyforgeops.api.domain.model.ReviewDashboard;
import com.symphonyforgeops.api.domain.model.ReviewFinding;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GovernanceService {

    private final JdbcTemplate jdbcTemplate;
    private final WorkOrderService workOrderService;

    public GovernanceService(JdbcTemplate jdbcTemplate, WorkOrderService workOrderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.workOrderService = workOrderService;
    }

    @Transactional
    public AgentAdapterConfig saveAdapter(String agentName, String adapterType, boolean enabled, String smokeStatus, String smokeReason) {
        jdbcTemplate.update("""
                        INSERT INTO agent_adapter_configs (
                            agent_name, adapter_type, enabled, smoke_status, smoke_reason, session_metadata
                        )
                        VALUES (?, ?, ?, ?, ?, JSON_OBJECT())
                        ON DUPLICATE KEY UPDATE
                            adapter_type = VALUES(adapter_type),
                            enabled = VALUES(enabled),
                            smoke_status = VALUES(smoke_status),
                            smoke_reason = VALUES(smoke_reason)
                        """,
                agentName,
                adapterType,
                enabled,
                smokeStatus,
                smokeReason);
        return new AgentAdapterConfig(agentName, adapterType, enabled, smokeStatus, smokeReason);
    }

    @Transactional
    public GithubLink saveGithubLink(String workOrderId, String issueUrl, String prUrl, String checkStatus) {
        jdbcTemplate.update("""
                        INSERT INTO github_links (work_order_id, issue_url, pr_url, check_status)
                        VALUES (?, ?, ?, ?)
                        """,
                workOrderService.numericId(workOrderId),
                issueUrl,
                prUrl,
                checkStatus);
        return new GithubLink(workOrderId, issueUrl, prUrl, checkStatus);
    }

    public List<GithubLink> githubLinks() {
        return jdbcTemplate.query("""
                        SELECT wo.work_order_key, gl.issue_url, gl.pr_url, gl.check_status
                        FROM github_links gl
                        JOIN work_orders wo ON wo.id = gl.work_order_id
                        ORDER BY gl.id
                        """,
                (rs, rowNum) -> new GithubLink(
                        rs.getString("work_order_key"),
                        rs.getString("issue_url"),
                        rs.getString("pr_url"),
                        rs.getString("check_status")
                ));
    }

    @Transactional
    public ReviewFinding saveFinding(String workOrderId, String source, String severity, String summary, String status) {
        jdbcTemplate.update("""
                        INSERT INTO review_findings (work_order_id, source, severity, summary, status)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                workOrderService.numericId(workOrderId),
                source,
                severity,
                summary,
                status);
        return new ReviewFinding(workOrderId, source, severity, summary, status);
    }

    @Transactional
    public HumanDecision saveHumanDecision(String workOrderId, String decision, String reason, String decider) {
        jdbcTemplate.update("""
                        INSERT INTO human_decisions (work_order_id, decision, reason, decider)
                        VALUES (?, ?, ?, ?)
                        """,
                workOrderService.numericId(workOrderId),
                decision,
                reason,
                decider);
        return new HumanDecision(workOrderId, decision, reason, decider);
    }

    public ReviewDashboard reviews() {
        return new ReviewDashboard(findings(), humanDecisions());
    }

    private List<ReviewFinding> findings() {
        return jdbcTemplate.query("""
                        SELECT wo.work_order_key, rf.source, rf.severity, rf.summary, rf.status
                        FROM review_findings rf
                        JOIN work_orders wo ON wo.id = rf.work_order_id
                        ORDER BY rf.id
                        """,
                (rs, rowNum) -> new ReviewFinding(
                        rs.getString("work_order_key"),
                        rs.getString("source"),
                        rs.getString("severity"),
                        rs.getString("summary"),
                        rs.getString("status")
                ));
    }

    private List<HumanDecision> humanDecisions() {
        return jdbcTemplate.query("""
                        SELECT wo.work_order_key, hd.decision, hd.reason, hd.decider
                        FROM human_decisions hd
                        JOIN work_orders wo ON wo.id = hd.work_order_id
                        ORDER BY hd.id
                        """,
                (rs, rowNum) -> new HumanDecision(
                        rs.getString("work_order_key"),
                        rs.getString("decision"),
                        rs.getString("reason"),
                        rs.getString("decider")
                ));
    }
}
