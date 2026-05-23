package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.WorkflowService;
import com.symphonyforgeops.api.domain.model.WorkflowContract;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/reload")
    public ApiResponse<WorkflowContract> reload(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(workflowService.reload(request.get("projectId")));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<WorkflowContract> get(@PathVariable String projectId) {
        return ApiResponse.ok(workflowService.get(projectId));
    }
}
