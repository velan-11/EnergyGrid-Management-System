package com.cts.billing_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id") private Long auditId;
    @Column(name = "user_id") private Long userId;
    @Column(name = "user_name", length = 120) private String userName;
    @Column(nullable = false, length = 50) private String action;
    @Column(name = "resource_type", nullable = false, length = 100) private String resourceType;
    @Column(name = "resource_id") private Long resourceId;
    @Column(columnDefinition = "TEXT") private String details;
    @Column(name = "service_name", length = 60) private String serviceName;
    @Column(nullable = false) private LocalDateTime timestamp;

    @PrePersist void onCreate() { if (timestamp == null) timestamp = LocalDateTime.now(); }

    public Long getAuditId() { return auditId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public Long getResourceId() { return resourceId; }
    public String getDetails() { return details; }
    public String getServiceName() { return serviceName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setUserId(Long v) { userId = v; }
    public void setUserName(String v) { userName = v; }
    public void setAction(String v) { action = v; }
    public void setResourceType(String v) { resourceType = v; }
    public void setResourceId(Long v) { resourceId = v; }
    public void setDetails(String v) { details = v; }
    public void setServiceName(String v) { serviceName = v; }
    public void setTimestamp(LocalDateTime v) { timestamp = v; }
}
