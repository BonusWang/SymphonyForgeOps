package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.service.CommandService;
import com.symphonyforgeops.api.domain.model.CommandRun;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/commands")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/runs")
    public ApiResponse<CommandRun> createRun(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(commandService.record(
                String.valueOf(request.get("projectId")),
                String.valueOf(request.get("workOrderId")),
                String.valueOf(request.get("command")),
                Boolean.TRUE.equals(request.get("destructive")),
                Boolean.TRUE.equals(request.get("approved"))
        ));
    }
}
