package com.cts.scheduling_service.dto.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GenerationScheduleResponseDTO {
    private Long scheduleId;
    private Long assetId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal targetKw;
    private String createdBy;
    private LocalDateTime createdAt;
    private String status;

    public GenerationScheduleResponseDTO() {
    }

    public GenerationScheduleResponseDTO(Long scheduleId, Long assetId, LocalDateTime startAt,
                                        LocalDateTime endAt, BigDecimal targetKw, String createdBy,
                                        LocalDateTime createdAt, String status) {
        this.scheduleId = scheduleId;
        this.assetId = assetId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.targetKw = targetKw;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public BigDecimal getTargetKw() {
        return targetKw;
    }

    public void setTargetKw(BigDecimal targetKw) {
        this.targetKw = targetKw;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

