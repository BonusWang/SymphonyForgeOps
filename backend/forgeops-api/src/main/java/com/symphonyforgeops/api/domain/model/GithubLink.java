package com.symphonyforgeops.api.domain.model;

public record GithubLink(
        String workOrderId,
        String issueUrl,
        String prUrl,
        String checkStatus
) {
}
