package com.cts.workorder_service.controller;

import com.cts.workorder_service.dto.RequestDTO
        .WorkOrderRequestDTO;
import com.cts.workorder_service.entity.WorkOrder;
import com.cts.workorder_service.exception
        .BadRequestException;
import com.cts.workorder_service.service
        .WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost
        .PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService service;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<WorkOrder>
    createWorkOrder(
            @Valid @RequestBody
            WorkOrderRequestDTO dto) {
        return ResponseEntity.ok(
                service.createWorkOrder(dto));
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR'," +
                    "'TECHNICIAN','AUDITOR')")
    public ResponseEntity<List<WorkOrder>>
    getAllWorkOrders() {
        return ResponseEntity.ok(
                service.getAllWorkOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR'," +
                    "'TECHNICIAN','AUDITOR')")
    public ResponseEntity<WorkOrder>
    getWorkOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                service.getWorkOrder(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<WorkOrder>
    updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody
            WorkOrderRequestDTO dto) {
        return ResponseEntity.ok(
                service.updateWorkOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Map<String, String>>
    deleteWorkOrder(
            @PathVariable Long id) {
        service.deleteWorkOrder(id);
        return ResponseEntity.ok(
                Map.of("message",
                        "Work order deleted successfully"));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<WorkOrder>
    assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId,
            // Optional — the frontend picker has the user's name from
            // identity-service. We pass it through so workorder_db can
            // cache the right label on the auto-created Technician row
            // instead of "Technician 42".
            @RequestParam(required = false) String technicianName) {

        if (technicianId == null) {
            throw new BadRequestException(
                    "Technician ID is required");
        }

        return ResponseEntity.ok(
                service.assignTechnician(
                        id, technicianId, technicianName));
    }

    /**
     * Update the work order's lifecycle status (OPEN / IN_PROGRESS /
     * COMPLETED / CANCELLED).
     *
     * TECHNICIAN is on the allowlist because the assigned technician
     * is the one who knows when the job is done — previously they had
     * to ping an operator to close the ticket because no endpoint
     * existed at all.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<WorkOrder>
    updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                service.updateStatus(id, status));
    }
}