package com.cts.outage_service.dto.RequestDTO;
import jakarta.validation.constraints.*;


import lombok.Data;

@Data
public class IncidentTaskDTO {

    @NotNull(message = "OutageId is required")
    private Long outageId;

    @NotNull(message = "Assigned user is required")
    private Long assignedTo;

    @NotBlank(message = "Evidence URL cannot be empty")
    private String evidenceURI;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "ASSIGNED|IN_PROGRESS|COMPLETED",
            message = "Status must be ASSIGNED, IN_PROGRESS or COMPLETED"
    )
    private String status;
}
