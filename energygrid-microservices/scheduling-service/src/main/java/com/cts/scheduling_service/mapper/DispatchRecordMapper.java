package com.cts.scheduling_service.mapper;

import com.cts.scheduling_service.dto.ResponseDTO.DispatchRecordResponseDTO;
import com.cts.scheduling_service.entity.DispatchRecord;
import org.springframework.stereotype.Component;

@Component
public class DispatchRecordMapper {

    public DispatchRecordResponseDTO toResponseDTO(DispatchRecord entity) {
        if (entity == null) {
            return null;
        }

        DispatchRecordResponseDTO dto = new DispatchRecordResponseDTO();
        dto.setDispatchId(entity.getDispatchId());
        dto.setScheduleId(entity.getScheduleId());
        dto.setExecutedAt(entity.getExecutedAt());
        dto.setExecutedBy(entity.getExecutedBy());
        dto.setExecutedByName(entity.getExecutedByName());
        dto.setExecutedByUsername(entity.getExecutedByUsername());
        dto.setActualKw(entity.getActualKw());
        dto.setTargetKw(entity.getTargetKw());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : null);
        dto.setNotes(entity.getNotes());

        return dto;
    }

    public DispatchRecord toEntity(DispatchRecordResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        DispatchRecord entity = new DispatchRecord();
        entity.setDispatchId(dto.getDispatchId());
        entity.setScheduleId(dto.getScheduleId());
        entity.setExecutedAt(dto.getExecutedAt());
        entity.setExecutedBy(dto.getExecutedBy());
        entity.setActualKw(dto.getActualKw());
        entity.setNotes(dto.getNotes());

        return entity;
    }
}

