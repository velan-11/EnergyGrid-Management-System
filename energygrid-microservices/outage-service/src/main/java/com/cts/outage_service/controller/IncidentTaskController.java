package com.cts.outage_service.controller;

import com.cts.outage_service.dto.RequestDTO.IncidentTaskDTO;
import com.cts.outage_service.entity.IncidentTask;
import com.cts.outage_service.service.IncidentTaskService;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for incident tasks - the field work units attached to an
 * outage. Covers create, lookup, status transitions and evidence upload.
 */
@RestController
@RequestMapping("/api/incident-tasks")
@RequiredArgsConstructor
public class IncidentTaskController {

    private final IncidentTaskService incidentTaskService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<IncidentTask> createIncidentTask(@RequestBody IncidentTaskDTO dto) {
        return ResponseEntity.ok(incidentTaskService.createIncidentTask(dto));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','AUDITOR')")
    public ResponseEntity<IncidentTask> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentTaskService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','AUDITOR')")
    public ResponseEntity<List<IncidentTask>> getAllTasks() {
        return ResponseEntity.ok(incidentTaskService.getAll());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<IncidentTask> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(incidentTaskService.updateStatus(id, status));
    }
    @PutMapping("/{id}/evidence")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<IncidentTask>
    updateEvidence(
            @PathVariable Long id,
            @RequestParam String evidenceURI){
        return ResponseEntity.ok(

                incidentTaskService.updateEvidence(id, evidenceURI)
        );
    }

}

