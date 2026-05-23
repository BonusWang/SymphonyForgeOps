package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.DashboardQueryService;
import com.symphonyforgeops.api.domain.model.DashboardSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    public DashboardController(DashboardQueryService dashboardQueryService) {
        this.dashboardQueryService = dashboardQueryService;
    }

    @GetMapping
    public DashboardSnapshot getDashboard() {
        return dashboardQueryService.snapshot();
    }
}

