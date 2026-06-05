package com.cts.scheduling_service.mapper;

import com.cts.scheduling_service.dto.ResponseDTO.GenerationScheduleResponseDTO;
import com.cts.scheduling_service.entity.GenerationSchedule;
import org.springframework.stereotype.Component;

/**
 * Converts between GenerationSchedule entities and their response DTOs.
 * Stringifies the status enum and guards against null entities/DTOs.
 */
@Component
public class GenerationScheduleMapper {

    public GenerationScheduleResponseDTO toResponseDTO(GenerationSchedule entity) {
        if (entity == null) {
            return null;
        }

        GenerationScheduleResponseDTO dto = new GenerationScheduleResponseDTO();
        dto.setScheduleId(entity.getScheduleId());
        dto.setAssetId(entity.getAssetId());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setTargetKw(entity.getTargetKw());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : null);

        return dto;
    }

    public GenerationSchedule toEntity(GenerationScheduleResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        GenerationSchedule entity = new GenerationSchedule();
        entity.setScheduleId(dto.getScheduleId());
        entity.setAssetId(dto.getAssetId());
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setTargetKw(dto.getTargetKw());
        entity.setCreatedBy(dto.getCreatedBy());

        return entity;
    }
}

