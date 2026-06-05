package com.cts.scheduling_service.dto.RequestDTO;

import jakarta.validation.constraints.*;

public class DispatchRequestDto {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    // 0 is a valid value — it means the dispatch failed completely, which the
    // service maps to status = FAILED. The previous "greater than 0" constraint
    // rejected that case at the controller boundary and a real outage couldn't
    // be logged. Allow >= 0; the service still validates non-null.
    @NotNull(message = "Actual kW is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Actual kW must be 0 or greater")
    private Double actualKw;

    @NotNull(message = "Executed by user ID is required")
    private Long executedBy;

    /** Denormalised name/username so the dispatch list can render without
     *  a Feign call back to identity-service per row. Optional — falls back
     *  to "User #id" when not supplied. */
    private String executedByName;
    private String executedByUsername;

    @Size(max = 255, message = "Notes cannot exceed 255 characters")
    private String notes;

    // getters & setters
    public Long getScheduleId() { return scheduleId; }
    public Double getActualKw() { return actualKw; }
    public Long getExecutedBy() { return executedBy; }
    public String getExecutedByName() { return executedByName; }
    public String getExecutedByUsername() { return executedByUsername; }
    public String getNotes() { return notes; }

    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public void setActualKw(Double actualKw) { this.actualKw = actualKw; }
    public void setExecutedBy(Long executedBy) { this.executedBy = executedBy; }
    public void setExecutedByName(String executedByName) { this.executedByName = executedByName; }
    public void setExecutedByUsername(String executedByUsername) { this.executedByUsername = executedByUsername; }
    public void setNotes(String notes) { this.notes = notes; }
}



