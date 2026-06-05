package com.cts.outage_service.mapper;

import com.cts.outage_service.dto.RequestDTO.IncidentTaskDTO;
import com.cts.outage_service.entity.IncidentTask;

public class IncidentTaskMapper {

    public static IncidentTask toEntity(IncidentTaskDTO dto) {

        IncidentTask task = new IncidentTask();   // âœ… correct

        task.setOutageId(dto.getOutageId());
        task.setAssignedTo(dto.getAssignedTo());

        return task;
    }
}

