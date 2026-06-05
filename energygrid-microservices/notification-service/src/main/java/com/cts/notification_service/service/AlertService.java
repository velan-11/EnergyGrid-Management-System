package com.cts.notification_service.service;


import com.cts.notification_service.entity.IncidentTask;
import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.repository.IncidentTaskRepository;
import com.cts.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Periodically scans incident tasks and raises HIGH-severity alerts for any
 * task that has stayed in ASSIGNED state for too long (overdue).
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final IncidentTaskRepository incidentTaskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Runs every 1 minute; flags tasks assigned more than 24h ago and still untouched.
    @Scheduled(fixedRate = 60000)
    public void checkOverdueTasks() {

        List<IncidentTask> tasks = incidentTaskRepository.findAll();

        for (IncidentTask task : tasks) {

            if ("ASSIGNED".equals(task.getStatus()) &&
                    task.getAssignedAt() != null &&
                    task.getAssignedAt().isBefore(Instant.now().minusSeconds(86400))) {

                // Avoid duplicate alerts: only notify if no ALERT for this task exists yet.
                List<Notification> existing = notificationRepository.findByUserId(task.getAssignedTo());

                boolean alreadySent = existing.stream()
                        .anyMatch(n -> n.getEntityId().equals(task.getId())
                                && "ALERT".equals(n.getCategory()));

                if (!alreadySent) {
                    notificationService.createNotification(
                            task.getAssignedTo(),
                            task.getId(),
                            "Task is overdue",
                            "ALERT",
                            "HIGH"
                    );
                }
            }
        }
    }
}

