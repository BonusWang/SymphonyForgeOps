package com.symphonyforgeops.api.domain.model;

import java.util.List;

public record ManagedProject(
        String id,
        String name,
        String repoUrl,
        String localPath,
        String defaultBranch,
        String stack,
        String status,
        List<String> commandTemplates
) {
}

