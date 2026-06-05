package com.cts.outage_service.repository;

import com.cts.outage_service.entity.IncidentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IncidentTaskRepository extends JpaRepository<IncidentTask, Long> {

    List<IncidentTask> findByOutageId(Long outageId);

    @Modifying
    @Transactional
    @Query("DELETE FROM IncidentTask t WHERE t.outageId = :outageId")
    int deleteByOutageId(Long outageId);
}
