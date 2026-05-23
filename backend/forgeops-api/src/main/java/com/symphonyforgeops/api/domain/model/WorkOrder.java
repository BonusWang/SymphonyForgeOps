package com.symphonyforgeops.api.domain.model;

public record WorkOrder(
        String id,
        String projectId,
        String title,
        String status,
        String implementationAgent,
        String testCommand,
        String reviewCommand,
        boolean humanApprovalRequired
) {
}

