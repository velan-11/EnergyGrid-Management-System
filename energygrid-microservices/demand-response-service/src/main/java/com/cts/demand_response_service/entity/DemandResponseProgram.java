package com.cts.demand_response_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A demand-response program (e.g. peak shaving, load shifting) that events
 * run under. Enrollment criteria are stored as a raw JSON string; status
 * defaults to DRAFT on creation.
 */
@Entity
@Table(name = "demand_response_programs")
@Data
public class DemandResponseProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long programId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProgramType type;

    @Column(name = "enrollment_criteria_json", columnDefinition = "json", nullable = false)
    private String enrollmentCriteriaJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ENUMS */

    public enum ProgramType {
        PEAK_SHAVING,
        LOAD_SHIFTING,
        EMERGENCY_DR,
        PRICE_BASED
    }

    public enum ProgramStatus {
        DRAFT,
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }
}
