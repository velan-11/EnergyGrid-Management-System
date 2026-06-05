package com.cts.scheduling_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Self-contained audit-log row.
 *
 * Each microservice owns its own audit_logs table — no cross-service foreign
 * keys. The userId/userName fields are denormalised at write time so the
 * admin audit page can render rows from many services without a Feign
 * roundtrip per row.
 *
 * The frontend federates by calling each service's /api/audit endpoint in
 * parallel and merging on timestamp DESC.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    /** Subject — the user who took the action. Denormalised to avoid a
     *  Feign call when rendering. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 120)
    private String userName;

    /** Verb — CREATE, UPDATE, DELETE, EXECUTE, ASSIGN, REPORT, ... */
    @Column(nullable = false, length = 50)
    private String action;

    /** Object — the entity type (Schedule, DispatchRecord, Outage, ...). */
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    /** Free-form context. Often a JSON-serialised diff or summary. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Origin service — set by AuditService.log() so the frontend can render
     *  a "Service" badge on each row when federating across services. */
    @Column(name = "service_name", length = 60)
    private String serviceName;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    void onCreate() {
        if (this.timestamp == null) this.timestamp = LocalDateTime.now();
    }

    public Long getAuditId() { return auditId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public Long getResourceId() { return resourceId; }
    public String getDetails() { return details; }
    public String getServiceName() { return serviceName; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setAction(String action) { this.action = action; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public void setDetails(String details) { this.details = details; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
