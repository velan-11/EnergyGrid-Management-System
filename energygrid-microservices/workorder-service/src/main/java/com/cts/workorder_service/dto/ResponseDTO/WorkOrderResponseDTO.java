package com.cts.workorder_service.dto.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WorkOrderResponseDTO {

    private Long id;
    private Long assetId;
    private String issueDescription;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
}
