package com.cts.workorder_service.mapper;

import com.cts.workorder_service.dto.RequestDTO.EvidenceUploadDTO;
import com.cts.workorder_service.entity.MaintenanceEvidence;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EvidenceMapper {

    private final ModelMapper modelMapper;

    public EvidenceMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public MaintenanceEvidence toEntity(EvidenceUploadDTO dto) {
        return modelMapper.map(dto, MaintenanceEvidence.class);
    }
}

