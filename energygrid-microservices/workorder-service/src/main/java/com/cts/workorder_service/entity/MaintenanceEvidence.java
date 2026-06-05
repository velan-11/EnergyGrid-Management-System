package com.cts.workorder_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Evidence attached to a work order: the uploaded file URL, free-text notes,
 * a SHA-256 for tamper checking, and review status. The back-reference to the
 * owning work order is {@code @JsonIgnore}d to avoid serialisation cycles.
 */
@Entity
@Table(name = "maintenance_evidence")
@Data
public class MaintenanceEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evidenceUrl;
    private String notes;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String sha256;
    private String status;

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    @JsonIgnore
    private WorkOrder workOrder;

    @PrePersist
    public void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}
