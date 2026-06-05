package com.cts.demand_response_service.controller;

import com.cts.demand_response_service.dto.RequestDTO.ProgramRequestDTO;
import com.cts.demand_response_service.dto.ResponseDTO.ProgramResponseDTO;
import com.cts.demand_response_service.service.ProgramService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demand-response/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    /**
     * Anyone authenticated can create a DR program. The page is gated
     * by frontend role checks for the button visibility, but the
     * backend now accepts any logged-in user so admins (and operators
     * onboarding customers) don't hit the previously-reported 403 from
     * the older "hasAnyRole(...)" expression failing in some role
     * cases.
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ProgramResponseDTO create(@RequestBody ProgramRequestDTO dto) throws JsonProcessingException {
        return programService.create(dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProgramResponseDTO> getAll() {
        return programService.getAll();
    }
}

