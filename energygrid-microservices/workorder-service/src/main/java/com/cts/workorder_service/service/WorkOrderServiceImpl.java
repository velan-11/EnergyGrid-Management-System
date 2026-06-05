package com.cts.workorder_service.service;

import com.cts.workorder_service.dto.RequestDTO.WorkOrderRequestDTO;
import com.cts.workorder_service.entity.Technician;
import com.cts.workorder_service.entity.WorkOrder;
import com.cts.workorder_service.exception.WorkOrderNotFoundException;
import com.cts.workorder_service.mapper.WorkOrderMapper;
import com.cts.workorder_service.notification.NotificationPublisher;
import com.cts.workorder_service.repository.TechnicianRepository;
import com.cts.workorder_service.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Core work-order business logic: persistence, status normalisation,
 * technician assignment (with on-the-fly Technician caching), notification
 * fan-out, and audit logging for every mutating operation.
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    @Autowired
    private WorkOrderRepository repository;

    @Autowired
    private WorkOrderMapper mapper;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private NotificationPublisher notifications;

    @Autowired
    private AuditService audit;

    /** Canonical status set the service writes and the frontend renders.
     *  The legacy "CREATED" string was confusing and never appeared on the
     *  badge palette; treat it as an alias for OPEN here, and migrate any
     *  existing rows via the SQL in resources/db/migration/V2__work_order_status.sql.
     */
    private static final java.util.Set<String> ALLOWED_STATUSES =
            java.util.Set.of("OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED");

    private static String normaliseStatus(String s) {
        if (s == null || s.isBlank()) return "OPEN";
        String v = s.trim().toUpperCase();
        if ("CREATED".equals(v)) return "OPEN"; // legacy alias
        return ALLOWED_STATUSES.contains(v) ? v : "OPEN";
    }

    @Override
    public WorkOrder createWorkOrder(WorkOrderRequestDTO dto) {
        WorkOrder workOrder = mapper.toEntity(dto);
        workOrder.setId(null);
        // New work orders start OPEN. Mapper-supplied statuses are normalised.
        workOrder.setStatus(normaliseStatus(workOrder.getStatus()));
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setDueDate(dto.getDueDate());
        WorkOrder saved = repository.save(workOrder);
        audit.log(null, null, "CREATE", "WorkOrder", saved.getId(),
                "issue=" + saved.getIssueDescription() + ", due=" + saved.getDueDate());
        return saved;
    }

    @Override
    public List<WorkOrder> getAllWorkOrders() {
        return repository.findAll();
    }

    @Override
    public WorkOrder getWorkOrder(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));
    }

    @Override
    public WorkOrder updateWorkOrder(Long id, WorkOrderRequestDTO dto) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));
        workOrder.setIssueDescription(dto.getIssueDescription());
        workOrder.setDueDate(dto.getDueDate());
        WorkOrder saved = repository.save(workOrder);
        audit.log(null, null, "UPDATE", "WorkOrder", saved.getId(),
                "issue=" + saved.getIssueDescription() + ", due=" + saved.getDueDate());
        return saved;
    }

    @Override
    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));
        repository.delete(workOrder);
        audit.log(null, null, "DELETE", "WorkOrder", id, null);
    }

    @Override
    public WorkOrder assignTechnician(Long id, Long technicianId, String technicianName) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));

        // Upsert the technician row. The frontend passes the
        // identity-service userId directly, so we use it as our PK and
        // create the row if missing. This eliminates the perpetual
        // "Technician {id} not found" error users hit when an
        // identity-only technician had never been pre-provisioned in
        // workorder_db.
        Technician tech = technicianRepository.findById(technicianId)
                .orElseGet(() -> {
                    Technician t = new Technician();
                    t.setId(technicianId);
                    t.setName(
                            (technicianName != null && !technicianName.isBlank())
                                    ? technicianName.trim()
                                    : "Technician " + technicianId
                    );
                    return technicianRepository.save(t);
                });

        // Refresh the cached name when the picker supplies a newer value
        // (admins rename users; we want the latest label).
        if (technicianName != null && !technicianName.isBlank()
                && !technicianName.equals(tech.getName())) {
            tech.setName(technicianName.trim());
            tech = technicianRepository.save(tech);
        }

        workOrder.setTechnician(tech);
        // Assigning a technician moves the work order into IN_PROGRESS,
        // matching the canonical enum the frontend badge palette uses.
        workOrder.setStatus("IN_PROGRESS");
        WorkOrder saved = repository.save(workOrder);

        notifications.publish(
                technicianId,
                saved.getId(),
                "workOrder",
                "WORK_ORDER",
                "INFO",
                "MEDIUM",
                "Work Order " + saved.getId() + " assigned to you",
                "Issue: " + saved.getIssueDescription()
        );

        audit.log(null, null, "ASSIGN", "WorkOrder", saved.getId(),
                "assignedTo=" + tech.getName() + " (id=" + technicianId + ")");

        return saved;
    }

    @Override
    public WorkOrder updateStatus(Long id, String status) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));

        // Normalise + validate against the canonical set the frontend
        // renders. Anything else (blank, typos, removed legacy values)
        // gets rejected so the table never shows phantom statuses.
        String normalised = normaliseStatus(status);
        if (status == null || status.isBlank()
                || !ALLOWED_STATUSES.contains(normalised)) {
            throw new com.cts.workorder_service.exception.BadRequestException(
                    "Invalid status. Allowed: " + ALLOWED_STATUSES);
        }

        workOrder.setStatus(normalised);
        WorkOrder saved = repository.save(workOrder);

        audit.log(null, null, "STATUS", "WorkOrder", saved.getId(),
                "status=" + normalised);

        // Notify the assigned technician when the work order closes —
        // they may need to see that operators marked it COMPLETED.
        if ("COMPLETED".equals(normalised) && workOrder.getTechnician() != null) {
            notifications.publish(
                    workOrder.getTechnician().getId(),
                    saved.getId(),
                    "workOrder",
                    "WORK_ORDER",
                    "INFO",
                    "LOW",
                    "Work Order " + saved.getId() + " marked completed",
                    "Issue: " + saved.getIssueDescription()
            );
        }

        return saved;
    }
}

