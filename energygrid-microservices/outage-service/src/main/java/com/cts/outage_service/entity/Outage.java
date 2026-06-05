package com.cts.outage_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;
import java.util.List;

/**
 * An outage event. Lifecycle: OPEN -> IN_PROGRESS -> RESOLVED (-> CLOSED).
 * Affected assets are stored as a JSON string rather than a relation.
 */
@Entity
@Table(name = "outages")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Outage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String affectedAssetsJSON;

    private Instant reportedAt;

    /* Set when status transitions to RESOLVED. Used by the Reports page to
     * compute Mean Time To Restore (resolvedAt - reportedAt). Before this
     * field existed MTTR defaulted to (now - reportedAt), so it kept
     * climbing forever and the KPI looked perpetually bad. Nullable -
     * stays null while the outage is OPEN / IN_PROGRESS. */
    private Instant resolvedAt;

    @NotBlank
    private String severity;     // e.g., LOW, MEDIUM, HIGH, CRITICAL

    @NotNull
    private Long reportedBy;

    @NotBlank
    private String status;       // e.g., OPEN, IN_PROGRESS, RESOLVED, CLOSED


}
