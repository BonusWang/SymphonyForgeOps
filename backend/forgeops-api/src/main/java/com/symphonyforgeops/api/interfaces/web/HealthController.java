package com.symphonyforgeops.api.interfaces.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Value("${forgeops.version}")
    private String version;

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "service", "forgeops-api",
                "version", version
        ));
    }
}
