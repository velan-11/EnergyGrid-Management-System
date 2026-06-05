package com.cts.scheduling_service.controller;

import com.cts.scheduling_service.dto.RequestDTO.DispatchRequestDto;
import com.cts.scheduling_service.dto.ResponseDTO.DispatchRecordResponseDTO;
import com.cts.scheduling_service.mapper.DispatchRecordMapper;
import com.cts.scheduling_service.service.DispatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for executing dispatches against schedules and reading
 * dispatch history. Delegates all logic to DispatchService.
 */
@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService service;
    private final DispatchRecordMapper mapper;

    public DispatchController(DispatchService service, DispatchRecordMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<DispatchRecordResponseDTO> execute(@Valid @RequestBody DispatchRequestDto dto) {
        var record = service.executeDispatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(record));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','AUDITOR')")
    public ResponseEntity<List<DispatchRecordResponseDTO>> getAll() {
        var records = service.getAllDispatches();
        var dtos = records.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','AUDITOR')")
    public ResponseEntity<DispatchRecordResponseDTO> getById(@PathVariable Long id) {
        var record = service.getDispatchById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(record));
    }

    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','AUDITOR')")
    public ResponseEntity<List<DispatchRecordResponseDTO>> getBySchedule(@PathVariable Long scheduleId) {
        var records = service.getBySchedule(scheduleId);
        var dtos = records.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}



