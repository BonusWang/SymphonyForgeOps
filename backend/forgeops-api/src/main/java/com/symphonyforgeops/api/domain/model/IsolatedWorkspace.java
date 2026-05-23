package com.symphonyforgeops.api.domain.model;

public record IsolatedWorkspace(
        String workOrderId,
        String workspaceKey,
        String workspacePath,
        String status
) {
}
