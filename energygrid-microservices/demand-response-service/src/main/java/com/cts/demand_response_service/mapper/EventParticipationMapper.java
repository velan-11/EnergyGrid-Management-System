package com.cts.demand_response_service.mapper;

import com.cts.demand_response_service.dto.ResponseDTO.EventParticipationResponseDTO;
import com.cts.demand_response_service.entity.DemandResponseParticipation;

public class EventParticipationMapper {

    private EventParticipationMapper() {}

    public static EventParticipationResponseDTO toResponse(
            DemandResponseParticipation p) {

        return new EventParticipationResponseDTO(
                p.getParticipationId(),
                p.getEvent().getEventId(),
                p.getParticipantEmail(),   // ✅ FIXED
                p.getReportedReductionKW(),
                p.getVerifiedAt(),
                p.getStatus().name()
        );
    }
}