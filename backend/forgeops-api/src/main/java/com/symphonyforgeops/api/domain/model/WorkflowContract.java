package com.symphonyforgeops.api.domain.model;

import java.util.List;

public record WorkflowContract(
        String projectId,
        String workflowPath,
        String trackerKind,
        List<String> activeStates,
        List<String> terminalStates,
        int pollingIntervalMs,
        int maxConcurrentAgents,
        int maxTurns,
        String workspaceRoot,
        String promptBody,
        String status,
        boolean lastKnownGood
) {
}
