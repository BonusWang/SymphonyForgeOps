package com.symphonyforgeops.api.domain.model;

public record RunEvent(
        String workOrderId,
        String eventType,
        String message
) {
}
