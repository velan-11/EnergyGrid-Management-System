package com.cts.notification_service.dto.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for publishing a notification from another service. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long userId;
    private Long entityId;
    private String title;
    private String message;
    private String type;              // INFO / WARNING / ALERT / SUCCESS
    private String category;          // OUTAGE / TASK / SCHEDULE / WORK_ORDER / BILLING / SYSTEM
    private String relatedEntityType; // e.g. "outage", "schedule"
    private String severity;          // LOW / MEDIUM / HIGH / CRITICAL
}
