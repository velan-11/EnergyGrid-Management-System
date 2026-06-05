package com.cts.demand_response_service.dto.RequestDTO;

import com.cts.demand_response_service.entity.DemandResponseProgram.ProgramType;

public class ProgramRequestDTO {
    public String name;
    public ProgramType type;
    public Object enrollmentCriteriaJson;
}

