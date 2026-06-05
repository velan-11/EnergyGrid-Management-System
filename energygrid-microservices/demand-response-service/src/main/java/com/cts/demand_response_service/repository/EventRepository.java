package com.cts.demand_response_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.demand_response_service.entity.DemandResponseEvent;

public interface EventRepository extends JpaRepository<DemandResponseEvent, Long> {
}


