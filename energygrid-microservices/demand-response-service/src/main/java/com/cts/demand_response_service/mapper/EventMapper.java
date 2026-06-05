package com.cts.demand_response_service.mapper;

import com.cts.demand_response_service.dto.ResponseDTO.EventResponseDTO;
import com.cts.demand_response_service.entity.DemandResponseEvent;

public class EventMapper {

    private EventMapper() {}

    public static EventResponseDTO toResponse(DemandResponseEvent event) {
        return new EventResponseDTO(
                event.getEventId(),
                event.getEventName(),
                event.getProgram().getProgramId(),
                event.getStartAt(),
                event.getEndAt(),
                event.getTargetReductionKW(),
                event.getStatus(),
                event.getCreatedBy()
        );
    }
}

