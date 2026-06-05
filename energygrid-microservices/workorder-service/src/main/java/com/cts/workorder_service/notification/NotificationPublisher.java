package com.cts.workorder_service.notification;

import com.cts.workorder_service.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Fire-and-forget. Notification failures must not block work-order flow. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationClient client;

    public void publish(Long userId, Long entityId, String relatedEntityType,
                        String category, String type, String severity,
                        String title, String message) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("entityId", entityId);
            body.put("relatedEntityType", relatedEntityType);
            body.put("category", category);
            body.put("type", type);
            body.put("severity", severity);
            body.put("title", title);
            body.put("message", message);
            client.create(body);
        } catch (Exception e) {
            log.warn("notification publish failed: {}", e.getMessage());
        }
    }
}
