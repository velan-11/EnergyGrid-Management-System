package com.cts.demand_response_service.service;



import com.cts.demand_response_service.dto.RequestDTO.EventParticipationRequestDTO;
import com.cts.demand_response_service.dto.RequestDTO.ReportReductionDTO;
import com.cts.demand_response_service.dto.ResponseDTO.EventParticipationResponseDTO;
import com.cts.demand_response_service.entity.DemandResponseEvent;
import com.cts.demand_response_service.entity.DemandResponseParticipation;
import com.cts.demand_response_service.mapper.EventParticipationMapper;
import com.cts.demand_response_service.repository.EventRepository;
import com.cts.demand_response_service.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;

    // ✅ 1. JOIN EVENT
    public EventParticipationResponseDTO join(EventParticipationRequestDTO dto) {

        String email = getCurrentUserEmail();

        DemandResponseEvent event = eventRepository.findById(dto.eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // ✅ Check already joined
        participationRepository
                .findByEvent_EventIdAndParticipantEmail(dto.eventId, email)
                .ifPresent(p -> {
                    throw new RuntimeException("Already joined");
                });

        DemandResponseParticipation p = new DemandResponseParticipation();
        p.setEvent(event);

        // ✅ Store email instead of User
        p.setParticipantEmail(email);
        p.setCreatedBy(email);

        p.setStatus(DemandResponseParticipation.ParticipationStatus.REGISTERED);

        return EventParticipationMapper.toResponse(
                participationRepository.save(p)
        );
    }

    // ✅ 2. REPORT REDUCTION
    public EventParticipationResponseDTO report(Long participationId, ReportReductionDTO dto) {

        DemandResponseParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        p.setReportedReductionKW(dto.reportedReductionKW);
        p.setStatus(DemandResponseParticipation.ParticipationStatus.REPORTED);

        return EventParticipationMapper.toResponse(
                participationRepository.save(p)
        );
    }

    // ✅ 3. VERIFY
    public EventParticipationResponseDTO verify(Long participationId) {

        DemandResponseParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        p.setStatus(DemandResponseParticipation.ParticipationStatus.VERIFIED);
        p.setVerifiedAt(LocalDateTime.now());

        return EventParticipationMapper.toResponse(
                participationRepository.save(p)
        );
    }

    // ✅ 4. OPT OUT
    public EventParticipationResponseDTO optOut(Long participationId) {

        DemandResponseParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        p.setStatus(DemandResponseParticipation.ParticipationStatus.OPTED_OUT);
        p.setReportedReductionKW(null);
        p.setVerifiedAt(null);

        return EventParticipationMapper.toResponse(
                participationRepository.save(p)
        );
    }

    // ✅ 5. GET BY EVENT
    public List<EventParticipationResponseDTO> getByEvent(Long eventId) {
        return participationRepository.findByEvent_EventId(eventId)
                .stream()
                .map(EventParticipationMapper::toResponse)
                .toList();
    }

    // ✅ Helper method
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}