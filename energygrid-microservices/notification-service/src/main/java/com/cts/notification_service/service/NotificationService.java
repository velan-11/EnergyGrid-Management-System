package com.cts.notification_service.service;

import com.cts.notification_service.dto.RequestDTO.NotificationDTO;
import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core notification logic: persists notifications, exposes lookups, and handles
 * read-state transitions. Derives a display type from severity when not supplied.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuditService audit;

    /** Legacy minimal-arg overload, retained for older callers. */
    public void createNotification(Long userId, Long entityId,
                                   String message, String category, String severity) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setEntityId(entityId);
        n.setMessage(message);
        n.setCategory(category);
        n.setSeverity(severity);
        n.setType(inferTypeFromSeverity(severity));
        notificationRepository.save(n);
    }

    /** Rich create - used by inter-service publishers. */
    public Notification createNotification(NotificationDTO dto) {
        Notification n = new Notification();
        n.setUserId(dto.getUserId());
        n.setEntityId(dto.getEntityId());
        n.setTitle(dto.getTitle());
        n.setMessage(dto.getMessage());
        n.setCategory(dto.getCategory());
        n.setSeverity(dto.getSeverity());
        n.setRelatedEntityType(dto.getRelatedEntityType());
        n.setType(dto.getType() != null
                ? dto.getType()
                : inferTypeFromSeverity(dto.getSeverity()));
        Notification saved = notificationRepository.save(n);
        audit.log(saved.getUserId(), null, "PUBLISH", "Notification", saved.getId(),
                "category=" + saved.getCategory() + ", severity=" + saved.getSeverity());
        return saved;
    }

    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setStatus("READ");
        return notificationRepository.save(notification);
    }

    public int markAllAsRead() {
        return notificationRepository.markAllAsRead();
    }

    public int markAllAsReadForUser(Long userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }

    // Maps a severity level to the notification's display type (INFO/WARNING/ALERT).
    private static String inferTypeFromSeverity(String severity) {
        if (severity == null) return "INFO";
        switch (severity.toUpperCase()) {
            case "HIGH":
            case "CRITICAL": return "ALERT";
            case "MEDIUM":   return "WARNING";
            case "LOW":      return "INFO";
            default:         return "INFO";
        }
    }
}
