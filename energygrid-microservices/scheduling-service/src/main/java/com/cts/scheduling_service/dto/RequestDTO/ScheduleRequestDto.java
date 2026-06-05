package com.cts.scheduling_service.dto.RequestDTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScheduleRequestDto {

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startAt;

    @NotNull(message = "End time is required")
    private LocalDateTime endAt;

    @NotNull(message = "Target kW is required")
    @DecimalMin(value = "0.1", inclusive = true, message = "Target kW must be greater than 0")
    private BigDecimal targetKw;

    @NotNull(message = "Created by user ID is required")
    private String createdBy;

    // getters & setters (unchanged)
    public Long getAssetId() { return assetId; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public BigDecimal getTargetKw() { return targetKw; }
    public String getCreatedBy() { return createdBy; }

    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public void setTargetKw(BigDecimal targetKw) { this.targetKw = targetKw; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}



