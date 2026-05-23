package com.symphonyforgeops.api.domain.model;

import java.util.List;

public record DashboardSnapshot(
        int projectCount,
        int openWorkOrderCount,
        int runningAgentCount,
        int pendingReviewCount,
        List<ManagedProject> projects,
        List<WorkOrder> workOrders,
        List<AgentRun> agentRuns,
        List<ReviewItem> reviewItems
) {
}

