package com.cts.workorder_service.dto.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EvidenceUploadDTO {

    @NotNull(message = "WorkOrder ID is required")
    private Long workOrderId;

    @NotBlank(message = "Evidence URL is required")
    @Size(max = 500, message = "URL too long")
    private String evidenceUrl;

    @Size(max = 300, message = "Notes cannot exceed 300 characters")
    private String notes;

    private String uploadedBy; // optional
    private String status;     // optional
}
