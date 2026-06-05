package com.cts.notification_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long entityId; // Task ID / Outage ID / Invoice ID etc.

    @Column(length = 200)
    private String title;

    @Column(length = 1000)
    private String message;

    private String type;          // INFO / WARNING / ALERT / SUCCESS

    private String category;      // OUTAGE / TASK / SCHEDULE / WORK_ORDER / BILLING / SYSTEM

    private String relatedEntityType; // e.g. "asset", "outage", "schedule"

    private String severity;      // LOW / MEDIUM / HIGH / CRITICAL

    private String status = "UNREAD"; // UNREAD / READ

    private Instant createdAt;

    private Instant expiresAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = Instant.now();
        if (this.status == null) this.status = "UNREAD";
    }
}
