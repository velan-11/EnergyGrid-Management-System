package com.cts.scheduling_service.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A planned generation window for an asset (target output between startAt/endAt).
 * Status transitions PLANNED -> ACTIVE (on first dispatch) -> CANCELLED.
 */
@Entity
@Table(name = "generation_schedule")
public class GenerationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    // Column name is case-sensitive and must match the existing "AssetID" column.
    @Column(name = "AssetID")
    private Long assetId;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "target_kw")
    private BigDecimal targetKw;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ScheduleStatus.PLANNED;
        }
    }

    // ===== GETTERS =====

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public BigDecimal getTargetKw() {
        return targetKw;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    // ===== SETTERS =====

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void setTargetKw(BigDecimal targetKw) {
        this.targetKw = targetKw;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }
}



