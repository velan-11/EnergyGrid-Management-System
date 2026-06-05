package com.cts.notification_service.service;


import com.cts.notification_service.entity.IncidentTask;
import com.cts.notification_service.repository.IncidentTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

/**
 * Simulates incident-task lifecycle progression on a timer:
 * ASSIGNED -> IN_PROGRESS -> COMPLETED based on elapsed time since assignment.
 */
@Component
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final IncidentTaskRepository incidentTaskRepository;

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void autoUpdateTaskStatus() {

        List<IncidentTask> tasks = incidentTaskRepository.findAll();

        for (IncidentTask task : tasks) {

            // Skip completed tasks
            if ("COMPLETED".equals(task.getStatus())) continue;

            long minutes = Duration.between(task.getAssignedAt(), Instant.now()).toMinutes();

            // After 1 min -> IN_PROGRESS
            if (minutes >= 1 && "ASSIGNED".equals(task.getStatus())) {
                task.setStatus("IN_PROGRESS");
            }

            // After 2 min -> COMPLETED
            if (minutes >= 2 && "IN_PROGRESS".equals(task.getStatus())) {
                task.setStatus("COMPLETED");
                task.setCompletedAt(Instant.now());
            }

            incidentTaskRepository.save(task);
        }
    }
}


