package com.symphonyforgeops.api.domain.model;

public record AgentAdapterConfig(
        String agentName,
        String adapterType,
        boolean enabled,
        String smokeStatus,
        String smokeReason
) {
}
