package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.AgentRun;
import com.symphonyforgeops.api.domain.model.DashboardSnapshot;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import com.symphonyforgeops.api.domain.model.ReviewItem;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardQueryService {

    private final List<ManagedProject> projects = List.of(
            new ManagedProject(
                    "wikiforge",
                    "WikiForge",
                    "https://github.com/BonusWang/WikiForge",
                    "../WikiForge",
                    "main",
                    "Java 21 / Spring Boot / Vue 3 / MySQL",
                    "active",
                    List.of("mvn -B test", "npm run build", "docker compose -f deploy/docker-compose.dev.yml config")
            ),
            new ManagedProject(
                    "team-workload",
                    "team-workload",
                    "https://github.com/BonusWang/team-workload",
                    "",
                    "main",
                    "pending inspection",
                    "pending",
                    List.of()
            ),
            new ManagedProject(
                    "symphony-forgeops",
                    "SymphonyForgeOps",
                    "https://github.com/BonusWang/SymphonyForgeOps",
                    ".",
                    "main",
                    "Java 21 / Spring Boot / Vue 3",
                    "bootstrap",
                    List.of("mvn -B test", "npm run build")
            )
    );

    private final List<WorkOrder> workOrders = List.of(
            new WorkOrder(
                    "wo-mvp0-bootstrap",
                    "symphony-forgeops",
                    "Bootstrap personal development mission control",
                    "in_progress",
                    "Codex",
                    "mvn -B test && npm run build",
                    "manual review",
                    true
            ),
            new WorkOrder(
                    "wo-wikiforge-contract",
                    "wikiforge",
                    "Register WikiForge commands and review gates",
                    "ready",
                    "Codex",
                    "mvn -B test",
                    "PR-Agent later",
                    true
            )
    );

    private final List<AgentRun> agentRuns = List.of(
            new AgentRun(
                    "run-bootstrap-001",
                    "wo-mvp0-bootstrap",
                    "Codex",
                    "running",
                    "Creating the independent SymphonyForgeOps skeleton.",
                    "2026-05-23T21:45:00+08:00"
            )
    );

    private final List<ReviewItem> reviewItems = List.of(
            new ReviewItem(
                    "review-bootstrap-001",
                    "wo-mvp0-bootstrap",
                    "architecture",
                    "pending",
                    "medium",
                    "Confirm MVP remains independent from WikiForge and avoids premature service splitting."
            )
    );

    public DashboardSnapshot snapshot() {
        return new DashboardSnapshot(
                projects.size(),
                workOrders.size(),
                agentRuns.size(),
                reviewItems.size(),
                projects,
                workOrders,
                agentRuns,
                reviewItems
        );
    }
}

