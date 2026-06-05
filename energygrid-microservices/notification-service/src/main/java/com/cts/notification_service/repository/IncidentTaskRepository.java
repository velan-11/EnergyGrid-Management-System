package com.cts.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.notification_service.entity.IncidentTask;
public interface IncidentTaskRepository extends JpaRepository<IncidentTask, Long> {
}

