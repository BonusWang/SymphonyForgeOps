package com.symphonyforgeops.api.domain.model;

public record AgentRun(
        String id,
        String workOrderId,
        String agentName,
        String status,
        String summary,
        String startedAt
) {
}

