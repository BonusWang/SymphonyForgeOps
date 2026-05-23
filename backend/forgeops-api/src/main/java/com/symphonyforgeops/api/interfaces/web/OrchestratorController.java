package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.OrchestratorService;
import com.symphonyforgeops.api.domain.model.DispatchResult;
import com.symphonyforgeops.api.domain.model.RunEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orchestrator")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/dispatch")
    public ApiResponse<DispatchResult> dispatch(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(orchestratorService.dispatch(request.get("workOrderId")));
    }

    @GetMapping("/events/{workOrderId}")
    public ApiResponse<List<RunEvent>> events(@PathVariable String workOrderId) {
        return ApiResponse.ok(orchestratorService.events(workOrderId));
    }
}
