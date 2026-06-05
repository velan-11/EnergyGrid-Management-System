
package com.cts.demand_response_service.dto.RequestDTO;

import java.time.LocalDateTime;

public class EventRequestDTO {
    public String eventName;
    public Long programId;
    public LocalDateTime startAt;
    public LocalDateTime endAt;
    public Double targetReductionKW;
}
