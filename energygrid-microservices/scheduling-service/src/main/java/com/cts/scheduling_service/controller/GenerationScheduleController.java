package com.cts.scheduling_service.controller;

import com.cts.scheduling_service.dto.RequestDTO.ScheduleRequestDto;
import com.cts.scheduling_service.dto.ResponseDTO.GenerationScheduleResponseDTO;
import com.cts.scheduling_service.mapper.GenerationScheduleMapper;
import com.cts.scheduling_service.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for creating, querying and cancelling generation schedules.
 * Delegates all logic to SchedulingService.
 */
@RestController
@RequestMapping("/api/schedules")
public class GenerationScheduleController {

    private final SchedulingService service;
    private final GenerationScheduleMapper mapper;

    public GenerationScheduleController(SchedulingService service, GenerationScheduleMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<GenerationScheduleResponseDTO> create(@Valid @RequestBody ScheduleRequestDto dto) {
        var schedule = service.createSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(schedule));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','AUDITOR')")
    public ResponseEntity<List<GenerationScheduleResponseDTO>> getAll() {
        var schedules = service.getAllSchedules();
        var dtos = schedules.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','AUDITOR')")
    public ResponseEntity<GenerationScheduleResponseDTO> getById(@PathVariable Long id) {
        var schedule = service.getScheduleById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(schedule));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','AUDITOR')")
    public ResponseEntity<List<GenerationScheduleResponseDTO>> getByAsset(@PathVariable Long assetId) {
        var schedules = service.getSchedulesByAsset(assetId);
        var dtos = schedules.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<GenerationScheduleResponseDTO> cancel(@PathVariable Long id) {
        var schedule = service.cancelSchedule(id);
        return ResponseEntity.ok(mapper.toResponseDTO(schedule));
    }
}



