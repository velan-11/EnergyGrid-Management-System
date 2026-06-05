package com.cts.scheduling_service.dto.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DispatchRecordResponseDTO {
    private Long dispatchId;
    private Long scheduleId;
    private LocalDateTime executedAt;
    private Long executedBy;
    private String executedByName;
    private String executedByUsername;
    private BigDecimal actualKw;
    /** Snapshot of the schedule's targetKw at execute time — lets the frontend
     *  render "27.4 / 30.0 kW" without a second call. */
    private BigDecimal targetKw;
    private String status;
    private String notes;

    public DispatchRecordResponseDTO() {
    }

    public DispatchRecordResponseDTO(Long dispatchId, Long scheduleId, LocalDateTime executedAt, 
                                    Long executedBy, BigDecimal actualKw, String status, String notes) {
        this.dispatchId = dispatchId;
        this.scheduleId = scheduleId;
        this.executedAt = executedAt;
        this.executedBy = executedBy;
        this.actualKw = actualKw;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public Long getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(Long executedBy) {
        this.executedBy = executedBy;
    }

    public BigDecimal getActualKw() {
        return actualKw;
    }

    public void setActualKw(BigDecimal actualKw) {
        this.actualKw = actualKw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExecutedByName() { return executedByName; }
    public void setExecutedByName(String executedByName) { this.executedByName = executedByName; }

    public String getExecutedByUsername() { return executedByUsername; }
    public void setExecutedByUsername(String executedByUsername) { this.executedByUsername = executedByUsername; }

    public BigDecimal getTargetKw() { return targetKw; }
    public void setTargetKw(BigDecimal targetKw) { this.targetKw = targetKw; }
}

