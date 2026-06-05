package com.cts.workorder_service.dto.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkOrderRequestDTO {

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotBlank(message = "Issue description cannot be empty")
    @Size(min = 5, max = 200, message = "Issue must be 5-200 characters")
    private String issueDescription;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
}
