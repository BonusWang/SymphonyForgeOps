package com.symphonyforgeops.api.domain.model;

public record DispatchResult(
        String workOrderId,
        String workerKey,
        String status,
        int exitCode,
        String stdout,
        String stderr
) {
}
