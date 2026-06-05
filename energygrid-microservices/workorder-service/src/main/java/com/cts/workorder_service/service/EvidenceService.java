package com.cts.workorder_service.service;


import com.cts.workorder_service.dto.RequestDTO.EvidenceUploadDTO;
import com.cts.workorder_service.entity.MaintenanceEvidence;


public interface EvidenceService {
    MaintenanceEvidence uploadEvidence(EvidenceUploadDTO dto);
    MaintenanceEvidence getEvidence(Long id);
    MaintenanceEvidence updateEvidence(Long id, EvidenceUploadDTO dto);
    void deleteEvidence(Long id);
}

