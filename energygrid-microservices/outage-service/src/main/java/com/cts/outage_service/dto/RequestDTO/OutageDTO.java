package com.cts.outage_service.dto.RequestDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OutageDTO {

    @NotEmpty(message = "Affected assets cannot be empty")
    private List<@NotBlank(message = "Asset cannot be blank") String> affectedAssets;

    @NotBlank(message = "Severity is required")
    @Pattern(
            regexp = "LOW|MEDIUM|HIGH",
            message = "Severity must be LOW, MEDIUM, or HIGH"
    )
    private String severity;

    @NotNull(message = "ReportedBy is required")
    private Long reportedBy;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "OPEN|IN_PROGRESS|RESOLVED",
            message = "Invalid status"
    )
    private String status;
}
