package com.cts.workorder_service.controller;

import com.cts.workorder_service.dto.RequestDTO.EvidenceUploadDTO;
import com.cts.workorder_service.entity.MaintenanceEvidence;
import com.cts.workorder_service.exception.BadRequestException;
import com.cts.workorder_service.service.EvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService service;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<MaintenanceEvidence> uploadEvidence(
            @Valid @RequestBody EvidenceUploadDTO dto) {

        if (dto.getWorkOrderId() <= 0) {
            throw new BadRequestException(
                    "Invalid WorkOrder ID");
        }

        return ResponseEntity.ok(
                service.uploadEvidence(dto));
    }


    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','AUDITOR')")
    public ResponseEntity<MaintenanceEvidence> getEvidence(
            @PathVariable Long id) {

        if (id <= 0) {
            throw new BadRequestException(
                    "Invalid Evidence ID");
        }

        return ResponseEntity.ok(
                service.getEvidence(id));
    }


    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<MaintenanceEvidence> updateEvidence(
            @PathVariable Long id,
            @Valid @RequestBody EvidenceUploadDTO dto) {

        if (id <= 0) {
            throw new BadRequestException(
                    "Invalid Evidence ID");
        }

        return ResponseEntity.ok(
                service.updateEvidence(id, dto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<Map<String, String>> deleteEvidence(
            @PathVariable Long id) {

        if (id <= 0) {
            throw new BadRequestException(
                    "Invalid Evidence ID");
        }

        service.deleteEvidence(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Evidence deleted successfully"
                )
        );
    }
}