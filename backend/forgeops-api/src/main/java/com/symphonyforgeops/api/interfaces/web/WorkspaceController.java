package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.WorkspaceService;
import com.symphonyforgeops.api.domain.model.IsolatedWorkspace;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ApiResponse<IsolatedWorkspace> create(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(workspaceService.create(
                request.get("workOrderId"),
                request.get("workspaceKey"),
                request.get("relativePath")
        ));
    }
}
