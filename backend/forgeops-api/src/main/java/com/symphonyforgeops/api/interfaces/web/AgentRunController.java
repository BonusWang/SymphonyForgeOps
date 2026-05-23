package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.GovernanceService;
import com.symphonyforgeops.api.domain.model.AgentAdapterConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent-runs")
public class AgentRunController {

    private final GovernanceService governanceService;

    public AgentRunController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @PostMapping("/adapters")
    public ApiResponse<AgentAdapterConfig> saveAdapter(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(governanceService.saveAdapter(
                String.valueOf(request.get("agentName")),
                String.valueOf(request.get("adapterType")),
                !Boolean.FALSE.equals(request.get("enabled")),
                String.valueOf(request.getOrDefault("smokeStatus", "skipped")),
                String.valueOf(request.getOrDefault("smokeReason", "Not executed"))
        ));
    }
}
