package com.cts.demand_response_service.controller;
import com.cts.demand_response_service.dto.RequestDTO.EventParticipationRequestDTO;
import com.cts.demand_response_service.dto.ResponseDTO.EventParticipationResponseDTO;
import com.cts.demand_response_service.dto.RequestDTO.ReportReductionDTO;
import com.cts.demand_response_service.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/demand-response/participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService service;

    @PostMapping("/join")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','PRODUCER','CUSTOMER')")
    public EventParticipationResponseDTO join(
            @RequestBody EventParticipationRequestDTO dto) {
        return service.join(dto);
    }


    @PatchMapping("/{id}/report")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','PRODUCER','CUSTOMER')")
    public EventParticipationResponseDTO report(
            @PathVariable Long id,
            @RequestBody ReportReductionDTO dto) {
        return service.report(id, dto);
    }


    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public EventParticipationResponseDTO verify(@PathVariable Long id) {
        return service.verify(id);
    }


    @PatchMapping("/{id}/opt-out")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','PRODUCER','CUSTOMER')")
    public EventParticipationResponseDTO optOut(@PathVariable Long id) {
        return service.optOut(id);
    }


    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','AUDITOR')")
    public List<EventParticipationResponseDTO> getByEvent(
            @PathVariable Long eventId) {
        return service.getByEvent(eventId);
    }
}

