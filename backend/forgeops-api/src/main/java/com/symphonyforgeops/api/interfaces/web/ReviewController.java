package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.GovernanceService;
import com.symphonyforgeops.api.domain.model.HumanDecision;
import com.symphonyforgeops.api.domain.model.ReviewDashboard;
import com.symphonyforgeops.api.domain.model.ReviewFinding;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final GovernanceService governanceService;

    public ReviewController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @GetMapping
    public ApiResponse<ReviewDashboard> reviews() {
        return ApiResponse.ok(governanceService.reviews());
    }

    @PostMapping("/findings")
    public ApiResponse<ReviewFinding> saveFinding(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(governanceService.saveFinding(
                request.get("workOrderId"),
                request.get("source"),
                request.get("severity"),
                request.get("summary"),
                request.getOrDefault("status", "open")
        ));
    }

    @PostMapping("/human-decisions")
    public ApiResponse<HumanDecision> saveDecision(@RequestBody Map<String, String> request) {
        return ApiResponse.ok(governanceService.saveHumanDecision(
                request.get("workOrderId"),
                request.get("decision"),
                request.get("reason"),
                request.get("decider")
        ));
    }
}
