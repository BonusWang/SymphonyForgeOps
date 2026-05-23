package com.symphonyforgeops.api.domain.model;

public record ReviewItem(
        String id,
        String workOrderId,
        String type,
        String status,
        String riskLevel,
        String summary
) {
}

