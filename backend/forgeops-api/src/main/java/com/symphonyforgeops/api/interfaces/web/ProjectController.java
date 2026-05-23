package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.dto.ProjectRequest;
import com.symphonyforgeops.api.application.service.ProjectService;
import com.symphonyforgeops.api.domain.model.ManagedProject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<List<ManagedProject>> list() {
        return ApiResponse.ok(projectService.list());
    }

    @PostMapping
    public ApiResponse<ManagedProject> create(@RequestBody ProjectRequest request) {
        return ApiResponse.ok(projectService.create(request));
    }

    @GetMapping("/{projectKey}")
    public ApiResponse<ManagedProject> get(@PathVariable String projectKey) {
        return ApiResponse.ok(projectService.get(projectKey));
    }

    @PutMapping("/{projectKey}")
    public ApiResponse<ManagedProject> update(@PathVariable String projectKey, @RequestBody ProjectRequest request) {
        return ApiResponse.ok(projectService.update(projectKey, request));
    }

    @PostMapping("/{projectKey}/archive")
    public ApiResponse<ManagedProject> archive(@PathVariable String projectKey) {
        return ApiResponse.ok(projectService.archive(projectKey));
    }
}
