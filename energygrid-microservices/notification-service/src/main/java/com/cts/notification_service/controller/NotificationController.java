package com.cts.notification_service.controller;

import com.cts.notification_service.dto.RequestDTO.NotificationDTO;
import com.cts.notification_service.entity.Notification;
import com.cts.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for creating, listing, and managing the read-state of notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(
            @RequestBody NotificationDTO dto) {
        return ResponseEntity.ok(notificationService.createNotification(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','CUSTOMER','AUDITOR')")
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','CUSTOMER','AUDITOR')")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByUser(userId));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','CUSTOMER','AUDITOR')")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Bulk: mark every UNREAD notification as READ. If `userId` is provided as
     * a query param, only that user's notifications are touched.
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','TECHNICIAN','PRODUCER','CUSTOMER','AUDITOR')")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestParam(required = false) Long userId) {
        int updated = userId != null
                ? notificationService.markAllAsReadForUser(userId)
                : notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of(
                "updated", updated,
                "userId", userId == null ? "all" : userId
        ));
    }
}
