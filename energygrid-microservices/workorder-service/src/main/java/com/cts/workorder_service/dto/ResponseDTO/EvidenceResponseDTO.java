package com.cts.workorder_service.dto.ResponseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Outbound view of an evidence record (owning work order omitted). */
@Getter
@Setter
public class EvidenceResponseDTO {

    private Long id;
    private String evidenceUrl;
    private String notes;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String status;
}
