package com.symphonyforgeops.api.interfaces.web;

import com.symphonyforgeops.api.application.dto.WorkOrderRequest;
import com.symphonyforgeops.api.application.dto.WorkOrderStatusRequest;
import com.symphonyforgeops.api.application.service.WorkOrderService;
import com.symphonyforgeops.api.domain.model.WorkOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public ApiResponse<List<WorkOrder>> list() {
        return ApiResponse.ok(workOrderService.list());
    }

    @PostMapping
    public ApiResponse<WorkOrder> create(@RequestBody WorkOrderRequest request) {
        return ApiResponse.ok(workOrderService.create(request));
    }

    @GetMapping("/{workOrderKey}")
    public ApiResponse<WorkOrder> get(@PathVariable String workOrderKey) {
        return ApiResponse.ok(workOrderService.get(workOrderKey));
    }

    @PostMapping("/{workOrderKey}/status")
    public ApiResponse<WorkOrder> updateStatus(
            @PathVariable String workOrderKey,
            @RequestBody WorkOrderStatusRequest request
    ) {
        return ApiResponse.ok(workOrderService.updateStatus(workOrderKey, request.status()));
    }
}
