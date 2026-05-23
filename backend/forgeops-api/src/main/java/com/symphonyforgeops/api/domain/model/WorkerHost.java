package com.symphonyforgeops.api.domain.model;

public record WorkerHost(
        String workerKey,
        String displayName,
        String protocol,
        String host,
        int capacity,
        String status,
        int availableCapacity,
        int currentRuns,
        String lastError
) {
}
