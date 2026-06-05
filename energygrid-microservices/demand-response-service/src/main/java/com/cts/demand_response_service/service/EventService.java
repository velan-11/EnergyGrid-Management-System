package com.cts.demand_response_service.service;


import com.cts.demand_response_service.dto.RequestDTO.EventRequestDTO;
import com.cts.demand_response_service.dto.ResponseDTO.EventResponseDTO;
import com.cts.demand_response_service.entity.*;
import com.cts.demand_response_service.mapper.EventMapper;
import com.cts.demand_response_service.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for DR events. Handles creation (linked to a program and
 * stamped with the caller's email) and lifecycle transitions between the
 * SCHEDULED / ACTIVE / COMPLETED / CANCELLED states.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ProgramRepository programRepository;

    // ✅ CREATE EVENT
    public EventResponseDTO create(EventRequestDTO dto) {

        // ✅ Get logged-in user email
        String email = getCurrentUserEmail();

        // ✅ Get program
        DemandResponseProgram program = programRepository.findById(dto.programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        // ✅ Create event
        DemandResponseEvent event = new DemandResponseEvent();
        event.setEventName(dto.eventName);
        event.setProgram(program);
        event.setStartAt(dto.startAt);
        event.setEndAt(dto.endAt);
        event.setTargetReductionKW(dto.targetReductionKW);

        // ✅ Store email instead of User
        event.setCreatedBy(email);

        return EventMapper.toResponse(
                eventRepository.save(event)
        );
    }

    // ✅ GET ALL EVENTS
    public List<EventResponseDTO> getAll() {
        return eventRepository.findAll()
                .stream()
                .map(EventMapper::toResponse)
                .toList();
    }

    // ✅ ACTIVATE EVENT
    public String activateEvent(Long id) {
        DemandResponseEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setStatus(DemandResponseEvent.EventStatus.ACTIVE);
        eventRepository.save(event);

        return event.getStatus().name();
    }

    // ✅ COMPLETE EVENT
    public String completeEvent(Long id) {
        DemandResponseEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setStatus(DemandResponseEvent.EventStatus.COMPLETED);
        eventRepository.save(event);

        return event.getStatus().name();
    }

    // ✅ CANCEL EVENT
    public String cancelEvent(Long id) {
        DemandResponseEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setStatus(DemandResponseEvent.EventStatus.CANCELLED);
        eventRepository.save(event);

        return event.getStatus().name();
    }

    // ✅ Helper method
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}