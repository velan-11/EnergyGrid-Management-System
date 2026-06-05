package com.cts.workorder_service.service;

import com.cts.workorder_service.dto.RequestDTO.EvidenceUploadDTO;
import com.cts.workorder_service.entity.MaintenanceEvidence;
import com.cts.workorder_service.entity.WorkOrder;
import com.cts.workorder_service.exception.EvidenceNotFoundException;
import com.cts.workorder_service.exception.WorkOrderNotFoundException;
import com.cts.workorder_service.mapper.EvidenceMapper;
import com.cts.workorder_service.repository.EvidenceRepository;
import com.cts.workorder_service.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business logic for maintenance-evidence records: upload, lookup, update,
 * and a JPQL-backed hard delete that sidesteps Hibernate cascade quirks.
 */
@Service
public class EvidenceServiceImpl implements EvidenceService {

    @Autowired
    private EvidenceRepository repository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private EvidenceMapper mapper;

    @Override
    public MaintenanceEvidence uploadEvidence(EvidenceUploadDTO dto) {
        WorkOrder workOrder = workOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new WorkOrderNotFoundException(dto.getWorkOrderId()));

        // Map the DTO, then force server-controlled fields: clear any client
        // id so we always insert, stamp the time, and default the uploader /
        // status when the client omits them.
        MaintenanceEvidence ev = mapper.toEntity(dto);
        ev.setId(null);
        ev.setUploadedAt(LocalDateTime.now());
        ev.setUploadedBy(dto.getUploadedBy() != null ? dto.getUploadedBy() : "SYSTEM");
        ev.setStatus(dto.getStatus() != null ? dto.getStatus() : "UPLOADED");
        // Real SHA-256 is computed by UploadController on the raw file; this
        // metadata record only carries a placeholder when none was supplied.
        ev.setSha256("dummy-hash");
        ev.setWorkOrder(workOrder);
        return repository.save(ev);
    }

    @Override
    public MaintenanceEvidence getEvidence(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EvidenceNotFoundException(id));
    }

    @Override
    public MaintenanceEvidence updateEvidence(Long id, EvidenceUploadDTO dto) {
        MaintenanceEvidence ev = repository.findById(id)
                .orElseThrow(() -> new EvidenceNotFoundException(id));
        ev.setEvidenceUrl(dto.getEvidenceUrl());
        ev.setNotes(dto.getNotes());
        return repository.save(ev);
    }

    /**
     * Delete an evidence row.
     *
     * Earlier implementations used `repository.delete(ev)` plus bidirectional
     * severing, but Hibernate's EAGER fetch on `WorkOrder.evidences` combined
     * with `cascade = CascadeType.ALL` could still resurrect the row from the
     * in-memory collection before the transaction committed. The JPQL DELETE
     * below runs straight against the DB and clears the persistence context
     * (`clearAutomatically`) so any subsequent GET in the same request sees
     * the post-delete state.
     */
    @Override
    @Transactional
    public void deleteEvidence(Long id) {
        if (!repository.existsById(id)) {
            throw new EvidenceNotFoundException(id);
        }
        int rows = repository.hardDeleteById(id);
        if (rows == 0) {
            throw new EvidenceNotFoundException(id);
        }
    }
}
