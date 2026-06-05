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

@Service
@RequiredArgsConstructor
public class AlertService {

    private final IncidentTaskRepository incidentTaskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void checkOverdueTasks() {

        List<IncidentTask> tasks = incidentTaskRepository.findAll();

        for (IncidentTask task : tasks) {

            if ("ASSIGNED".equals(task.getStatus()) &&
                    task.getAssignedAt() != null &&
                    task.getAssignedAt().isBefore(Instant.now().minusSeconds(86400))) {

                // âœ… CHECK DUPLICATE
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

