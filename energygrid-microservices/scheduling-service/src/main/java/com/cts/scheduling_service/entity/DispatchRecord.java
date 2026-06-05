package com.cts.scheduling_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * A single dispatch execution against a schedule. Stores the actual output, a
 * frozen snapshot of the schedule's target, the derived status, and denormalised
 * operator details so list views avoid extra lookups.
 */
@Entity
@Table(name = "dispatch_record")
public class DispatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispatch_id")
    private Long dispatchId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by")
    private Long executedBy;

    /** Denormalised user info so list responses don't need a Feign roundtrip
     *  per row. Populated at execute time from the request DTO. */
    @Column(name = "executed_by_name", length = 120)
    private String executedByName;

    @Column(name = "executed_by_username", length = 80)
    private String executedByUsername;

    @Column(name = "actual_kw", precision = 10, scale = 2)
    private BigDecimal actualKw;

    /** Snapshot of the linked schedule's targetKw at execute time. Frozen so a
     *  later schedule edit can't retroactively change reported efficiency. */
    @Column(name = "target_kw", precision = 10, scale = 2)
    private BigDecimal targetKw;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DispatchStatus status;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    public void prePersist() {
        this.executedAt = LocalDateTime.now();
    }

    // ================= GETTERS =================

    public Long getDispatchId() {
        return dispatchId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public Long getExecutedBy() {
        return executedBy;
    }

    public String getExecutedByName() { return executedByName; }
    public String getExecutedByUsername() { return executedByUsername; }

    public BigDecimal getActualKw() {
        return actualKw;
    }

    public BigDecimal getTargetKw() { return targetKw; }
    public void setTargetKw(BigDecimal targetKw) { this.targetKw = targetKw; }

    public DispatchStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    // ================= SETTERS =================

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public void setExecutedBy(Long executedBy) {
        this.executedBy = executedBy;
    }

    public void setExecutedByName(String executedByName) { this.executedByName = executedByName; }
    public void setExecutedByUsername(String executedByUsername) { this.executedByUsername = executedByUsername; }

    public void setActualKw(BigDecimal actualKw) {
        this.actualKw = actualKw;
    }

    public void setStatus(DispatchStatus status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

