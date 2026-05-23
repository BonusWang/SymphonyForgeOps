package com.symphonyforgeops.api.domain.model;

public record CommandRun(
        long id,
        String projectId,
        String workOrderId,
        String command,
        String status,
        String stdout,
        String stderr,
        Integer exitCode
) {
}
