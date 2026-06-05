package com.cts.demand_response_service.repository;

import com.cts.demand_response_service.entity.DemandResponseParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository
        extends JpaRepository<DemandResponseParticipation, Long> {



    List<DemandResponseParticipation>
    findByEvent_EventId(Long eventId);

    Optional<DemandResponseParticipation> findByEvent_EventIdAndParticipantEmail(Long eventId, String email);
}


