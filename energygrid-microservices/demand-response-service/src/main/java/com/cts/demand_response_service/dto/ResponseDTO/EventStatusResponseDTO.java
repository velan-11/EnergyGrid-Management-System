package com.cts.demand_response_service.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventStatusResponseDTO {
    private Long id;
    private String status;
}
