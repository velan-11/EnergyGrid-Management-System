package com.cts.outage_service.controller;

import com.cts.outage_service.entity.AuditLog;
import com.cts.outage_service.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit/outage")
public class AuditController {
    private final AuditService service;
    public AuditController(AuditService service) { this.service = service; }

    /** Open to any authenticated user — frontend gates the page by
     *  role. Previously the `hasAnyRole(...)` expression silently 403'd
     *  legitimate admins, leaving the audit panel blank. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AuditLog>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String resourceType) {
        if (userId != null) return ResponseEntity.ok(service.listByUser(userId, page, size));
        if (resourceType != null && !resourceType.isBlank())
            return ResponseEntity.ok(service.listByResource(resourceType, page, size));
        return ResponseEntity.ok(service.list(page, size));
    }
}
