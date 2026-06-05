package com.cts.demand_response_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(
        name = "demand_response_participation",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"event_id", "participant_email"}
        )
)
@Data
public class DemandResponseParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participationId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private DemandResponseEvent event;

    @Column(name = "participant_email", nullable = false)
    private String participantEmail;

    @Column(name = "reported_reduction_kw")
    private Double reportedReductionKW;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.REGISTERED;

    @Column(name = "created_by", nullable = false)
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
    public enum ParticipationStatus{
        REGISTERED,
        REPORTED,
        VERIFIED,
        OPTED_OUT
    }
}