package com.symphonyforgeops.api.application.dto;

public record WorkOrderRequest(
        String workOrderKey,
        String projectId,
        String title,
        String status,
        String implementationAgent,
        String testCommand,
        String reviewCommand,
        boolean humanApprovalRequired
) {
}
