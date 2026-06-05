package com.cts.demand_response_service.dto.ResponseDTO;

import com.cts.demand_response_service.entity.DemandResponseProgram.ProgramStatus;
import com.cts.demand_response_service.entity.DemandResponseProgram.ProgramType;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgramResponseDTO {

    private Long programId;
    private String name;
    private ProgramType type;
    private ProgramStatus status;

    @JsonRawValue
    private String enrollmentCriteriaJson;

    private String createdBy;
}

