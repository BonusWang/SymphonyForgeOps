package com.symphonyforgeops.api.application.service;

import com.symphonyforgeops.api.domain.model.CommandRun;
import com.symphonyforgeops.api.interfaces.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandService {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final WorkOrderService workOrderService;

    public CommandService(JdbcTemplate jdbcTemplate, ProjectService projectService, WorkOrderService workOrderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.workOrderService = workOrderService;
    }

    @Transactional
    public CommandRun record(String projectId, String workOrderId, String command, boolean destructive, boolean approved) {
        if (destructive && !approved) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "DESTRUCTIVE_COMMAND_REQUIRES_APPROVAL",
                    "Destructive command requires explicit approval."
            );
        }
        jdbcTemplate.update("""
                        INSERT INTO command_runs (
                            project_id, work_order_id, command_text, status, destructive, approved
                        )
                        VALUES (?, ?, ?, 'queued', ?, ?)
                        """,
                projectService.numericId(projectId),
                workOrderService.numericId(workOrderId),
                command,
                destructive,
                approved);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return new CommandRun(id == null ? 0 : id, projectId, workOrderId, command, "queued", null, null, null);
    }
}
