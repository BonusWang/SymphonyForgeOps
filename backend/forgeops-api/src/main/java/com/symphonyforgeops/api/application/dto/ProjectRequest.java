package com.symphonyforgeops.api.application.dto;

import java.util.List;

public record ProjectRequest(
        String projectKey,
        String name,
        String repoUrl,
        String localPath,
        String defaultBranch,
        String workflowPath,
        String workspaceRoot,
        String stack,
        String status,
        List<String> commandTemplates
) {
}
