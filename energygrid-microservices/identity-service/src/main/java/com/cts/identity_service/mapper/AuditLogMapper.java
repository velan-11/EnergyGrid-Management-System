package com.cts.identity_service.mapper;
import com.cts.identity_service.dto.ResponseDTO.AuditLogResponseDTO;
import com.cts.identity_service.entity.AuditLog;
/** Maps {@link AuditLog} entities (with their owning user) to response DTOs. */
public class AuditLogMapper {

    public static AuditLogResponseDTO toDTO(AuditLog auditLog) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setAuditId(auditLog.getAuditId());
        dto.setUserId(auditLog.getUser().getUserId());
        dto.setName(auditLog.getUser().getName());
        dto.setAction(auditLog.getAction());
        dto.setResourceType(auditLog.getResourceType());
        dto.setDetails(auditLog.getDetails());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}
