package com.cts.outage_service.controller;

import com.cts.outage_service.dto.RequestDTO.OutageDTO;
import com.cts.outage_service.entity.Outage;
import com.cts.outage_service.service.OutageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for outages: create, list, fetch, partial update and delete.
 * Role checks are enforced per-method via {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/outages")
@RequiredArgsConstructor
public class OutageController {

    private final OutageService outageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<Outage> create(@Valid @RequestBody OutageDTO dto) {
        Outage saved = outageService.createOutage(dto);
        return ResponseEntity.created(URI.create("/api/outages/" + saved.getId())).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','AUDITOR')")
    public List<Outage> list() {
        return outageService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','AUDITOR')")
    public ResponseEntity<Outage> get(@PathVariable Long id) {
        return ResponseEntity.ok(outageService.findById(id));
    }

    /**
     * Partial update: status / severity / notes. Body can contain any subset of
     * those three fields. Returns the updated outage.
     *
     * TECHNICIAN is included because the technician who closes the incident in
     * the field needs to flip the parent outage to RESOLVED. Restricting this
     * to ADMIN/OPERATOR forced the technician to ping someone every time they
     * finished a job, which was the source of the "edit denied" reports.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN')")
    public ResponseEntity<Outage> patch(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(outageService.patch(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        outageService.delete(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
