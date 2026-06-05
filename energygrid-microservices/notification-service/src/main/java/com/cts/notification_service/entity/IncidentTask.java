package com.cts.notification_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "incident_tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class IncidentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskid")
    private Long id;

    // Keeping this as a simple FK field per your spec
    @Column(name = "outage_id")
    private Long outageId;

    @NotNull
    private Long assignedTo;     // user id / name

    private Instant assignedAt;    // set on create if null
    private Instant completedAt;

    private String evidenceURI;

    @NotBlank
    private String status;
    // e.g., ASSIGNED, IN_PROGRESS, DONE
}

