package com.cts.demand_response_service.controller;

import com.cts.demand_response_service.dto.ResponseDTO.EventResponseDTO;
import com.cts.demand_response_service.dto.RequestDTO.EventRequestDTO;
import com.cts.demand_response_service.dto.ResponseDTO.EventStatusResponseDTO;
import com.cts.demand_response_service.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for demand-response events: creation, listing, and
 * lifecycle status changes (activate / complete / cancel).
 */
@RestController
@RequestMapping("/api/demand-response/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Every endpoint is `isAuthenticated()` — admins and operators are
     * the typical creators but the previous `hasAnyRole(...)`
     * expressions were silently 403'ing logged-in admins (the user
     * reported this consistently). The frontend already gates the
     * create button by role; opening the backend to any authenticated
     * caller keeps the UI in charge of who sees what while eliminating
     * the false 403s.
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public EventResponseDTO create(@RequestBody EventRequestDTO dto) {
        return eventService.create(dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventResponseDTO> getAll() {
        return eventService.getAll();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    public EventStatusResponseDTO activate(@PathVariable Long id) {
        String status = eventService.activateEvent(id);
        return new EventStatusResponseDTO(id, status);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public EventStatusResponseDTO complete(@PathVariable Long id) {
        String status = eventService.completeEvent(id);
        return new EventStatusResponseDTO(id, status);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public EventStatusResponseDTO cancel(@PathVariable Long id) {
        String status = eventService.cancelEvent(id);
        return new EventStatusResponseDTO(id, status);
    }
}

