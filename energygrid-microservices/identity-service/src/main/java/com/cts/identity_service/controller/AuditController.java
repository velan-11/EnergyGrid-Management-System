package com.cts.identity_service.controller;
import com.cts.identity_service.dto.ResponseDTO.AuditLogResponseDTO;
import com.cts.identity_service.mapper.AuditLogMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cts.identity_service.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/api/identity/audit")
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {

        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditLogResponseDTO> getAllAudits() {
        return auditService.getAllAuditLogs()
                .stream()
                .map(AuditLogMapper::toDTO)
                .toList();
    }

    @GetMapping("/{userId}")
    public List<AuditLogResponseDTO> getAuditsByUser(@PathVariable Long userId) {
        return auditService.getAuditLogsByUser(userId)
                .stream()
                .map(AuditLogMapper::toDTO)
                .toList();
    }
}
