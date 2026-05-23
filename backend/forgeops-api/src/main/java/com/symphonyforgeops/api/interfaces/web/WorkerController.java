package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.WorkerService;
import com.symphonyforgeops.api.domain.model.WorkerHost;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public ApiResponse<List<WorkerHost>> list() {
        return ApiResponse.ok(workerService.list());
    }

    @PostMapping
    public ApiResponse<WorkerHost> register(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(workerService.register(
                String.valueOf(request.get("workerKey")),
                String.valueOf(request.get("displayName")),
                String.valueOf(request.get("protocol")),
                String.valueOf(request.get("host")),
                integer(request.get("capacity"), 1),
                String.valueOf(request.getOrDefault("status", "offline"))
        ));
    }

    @PostMapping("/{workerKey}/heartbeat")
    public ApiResponse<WorkerHost> heartbeat(@PathVariable String workerKey, @RequestBody Map<String, Object> request) {
        Object lastError = request.get("lastError");
        return ApiResponse.ok(workerService.heartbeat(
                workerKey,
                String.valueOf(request.getOrDefault("status", "online")),
                integer(request.get("availableCapacity"), 0),
                integer(request.get("currentRuns"), 0),
                lastError == null ? null : String.valueOf(lastError)
        ));
    }

    private int integer(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
