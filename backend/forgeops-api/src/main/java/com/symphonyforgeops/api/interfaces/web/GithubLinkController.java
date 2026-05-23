package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.GovernanceService;
import com.symphonyforgeops.api.domain.model.GithubLink;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/github-links")
public class GithubLinkController {

    private final GovernanceService governanceService;

    public GithubLinkController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @GetMapping
    public ApiResponse<List<GithubLink>> list() {
        return ApiResponse.ok(governanceService.githubLinks());
    }

    @PostMapping
    public ApiResponse<GithubLink> save(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(governanceService.saveGithubLink(
                request.get("workOrderId"),
                request.get("issueUrl"),
                request.get("prUrl"),
                request.getOrDefault("checkStatus", "unknown")
        ));
    }
}
