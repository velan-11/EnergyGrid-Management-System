package com.cts.billing_service.notification;

import com.cts.billing_service.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationClient client;

    public void publish(Long userId, Long entityId, String relatedEntityType,
                        String category, String type, String severity,
                        String title, String message) {
        Map<String, Object> body = buildBody(userId, entityId, relatedEntityType,
                category, type, severity, title, message);
        try {
            client.create(body);
        } catch (Exception e) {
            log.warn("notification publish failed: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildBody(Long userId, Long entityId, String relatedEntityType,
                                          String category, String type, String severity,
                                          String title, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("entityId", entityId);
        body.put("relatedEntityType", relatedEntityType);
        body.put("category", category);
        body.put("type", type);
        body.put("severity", severity);
        body.put("title", title);
        body.put("message", message);
        return body;
    }
}
