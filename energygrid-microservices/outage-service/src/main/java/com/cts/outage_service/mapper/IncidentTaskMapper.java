package com.cts.outage_service.mapper;

import com.cts.outage_service.dto.RequestDTO.IncidentTaskDTO;
import com.cts.outage_service.entity.IncidentTask;

/** Maps an incoming IncidentTaskDTO to a new IncidentTask entity. */
public class IncidentTaskMapper {

    public static IncidentTask toEntity(IncidentTaskDTO dto) {
        // Only the FK and assignee come from the request; lifecycle fields
        // (assignedAt, status, ...) are stamped by the service layer.
        IncidentTask task = new IncidentTask();
        task.setOutageId(dto.getOutageId());
        task.setAssignedTo(dto.getAssignedTo());
        return task;
    }
}

