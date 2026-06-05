package com.cts.workorder_service.service;

import com.cts.workorder_service.dto.RequestDTO.WorkOrderRequestDTO;
import com.cts.workorder_service.entity.WorkOrder;
import java.util.List;

public interface WorkOrderService {
    WorkOrder createWorkOrder(WorkOrderRequestDTO dto);
    List<WorkOrder> getAllWorkOrders();
    WorkOrder getWorkOrder(Long id);
    WorkOrder updateWorkOrder(Long id, WorkOrderRequestDTO dto);
    void deleteWorkOrder(Long id);
    /**
     * Assign a technician by their identity-service userId. If the
     * workorder_db doesn't yet have a Technician row for that user we
     * create one on the fly using the supplied name (the frontend
     * already has it from the picker). `technicianName` is optional —
     * falls back to "Technician {id}" if blank.
     */
    WorkOrder assignTechnician(Long id, Long technicianId, String technicianName);

    /**
     * Update the work order's lifecycle status. Allowed values match the
     * canonical set the frontend renders: OPEN, IN_PROGRESS, COMPLETED,
     * CANCELLED. Any other value is rejected.
     */
    WorkOrder updateStatus(Long id, String status);
}

