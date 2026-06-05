package com.cts.demand_response_service.dto.ResponseDTO;

import com.cts.demand_response_service.entity.DemandResponseEvent.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EventResponseDTO {

    private Long eventId;
    private String eventName;
    private Long programId;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Double targetReductionKW;

    private EventStatus status;
    private String createdBy;
}


