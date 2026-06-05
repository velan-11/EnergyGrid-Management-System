package com.cts.demand_response_service.mapper;


import com.cts.demand_response_service.dto.ResponseDTO.ProgramResponseDTO;
import com.cts.demand_response_service.entity.DemandResponseProgram;

public class ProgramMapper {

    private ProgramMapper() {}

    public static ProgramResponseDTO toResponse(DemandResponseProgram program) {
        return new ProgramResponseDTO(
                program.getProgramId(),
                program.getName(),
                program.getType(),
                program.getStatus(),
                program.getEnrollmentCriteriaJson(),
                program.getCreatedBy()
        );
    }
}

