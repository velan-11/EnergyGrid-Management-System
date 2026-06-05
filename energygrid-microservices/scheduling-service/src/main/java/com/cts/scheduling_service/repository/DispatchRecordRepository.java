package com.cts.scheduling_service.repository;

import com.cts.scheduling_service.entity.DispatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchRecordRepository
        extends JpaRepository<DispatchRecord, Long> {

    // âœ… REQUIRED for: GET /api/dispatch/schedule/{scheduleId}
    List<DispatchRecord> findByScheduleId(Long scheduleId);
}



