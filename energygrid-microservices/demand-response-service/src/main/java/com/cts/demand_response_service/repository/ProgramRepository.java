package com.cts.demand_response_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.demand_response_service.entity.DemandResponseProgram;

public interface ProgramRepository extends JpaRepository<DemandResponseProgram, Long> {
}

