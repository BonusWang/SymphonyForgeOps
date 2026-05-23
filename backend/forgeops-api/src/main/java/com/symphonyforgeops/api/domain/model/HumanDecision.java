package com.symphonyforgeops.api.domain.model;

public record HumanDecision(
        String workOrderId,
        String decision,
        String reason,
        String decider
) {
}
