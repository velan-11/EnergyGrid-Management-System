package com.cts.identity_service.dto.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLogResponseDTO {

    private Long auditId;
    private Long userId;
    private String name;
    private String action;
    private String resourceType;
    private String details;
    private LocalDateTime timestamp;
}
