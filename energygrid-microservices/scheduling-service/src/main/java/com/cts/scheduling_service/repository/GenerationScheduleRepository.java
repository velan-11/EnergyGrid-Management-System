package com.cts.scheduling_service.repository;

import com.cts.scheduling_service.entity.GenerationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerationScheduleRepository
        extends JpaRepository<GenerationSchedule, Long> {

    // Backs GET /api/schedules/asset/{assetId}
    List<GenerationSchedule> findByAssetId(Long assetId);
}




