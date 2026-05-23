package com.symphonyforgeops.api.domain.model;

public record ReviewFinding(
        String workOrderId,
        String source,
        String severity,
        String summary,
        String status
) {
}
