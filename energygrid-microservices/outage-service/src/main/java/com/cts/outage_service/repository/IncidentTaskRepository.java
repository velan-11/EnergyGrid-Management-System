package com.cts.outage_service.repository;

import com.cts.outage_service.entity.IncidentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Data access for incident tasks, keyed off the parent outage's FK. */
public interface IncidentTaskRepository extends JpaRepository<IncidentTask, Long> {

    List<IncidentTask> findByOutageId(Long outageId);

    // Bulk-delete all tasks for an outage; used by the outage cascade delete.

    @Modifying
    @Transactional
    @Query("DELETE FROM IncidentTask t WHERE t.outageId = :outageId")
    int deleteByOutageId(Long outageId);
}
