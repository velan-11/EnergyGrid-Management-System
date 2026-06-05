package com.cts.demand_response_service.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EventParticipationResponseDTO {

    private Long participationId;
    private Long eventId;
    private String participantEmail;
    private Double reportedReductionKW;
    private LocalDateTime verifiedAt;
    private String status;
}


