package com.cts.outage_service.service;

import com.cts.outage_service.client
        .AssetServiceClient;
import com.cts.outage_service.dto.RequestDTO.AssetDTO;
import com.cts.outage_service.dto.RequestDTO.OutageDTO;
import com.cts.outage_service.entity.Outage;
import com.cts.outage_service.exception
        .BadRequestException;
import com.cts.outage_service.notification.NotificationPublisher;
import com.cts.outage_service.repository.IncidentTaskRepository;
import com.cts.outage_service.repository
        .OutageRepository;
import com.fasterxml.jackson.databind
        .ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker
        .annotation.CircuitBreaker;
import io.github.resilience4j.retry
        .annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutageService {

    private final OutageRepository
            outageRepository;
    private final IncidentTaskRepository
            incidentTaskRepository;
    private final AssetServiceClient
            assetServiceClient;
    private final NotificationPublisher
            notifications;
    private final AuditService audit;

    // ✅ Circuit Breaker + Retry
    @CircuitBreaker(
            name = "assetService",
            fallbackMethod = "assetFallback"
    )
    @Retry(name = "assetService")
    public AssetDTO getAssetDetails(
            Long assetId) {
        System.out.println(
                "Calling asset-service for: "
                        + assetId);
        return assetServiceClient
                .getAssetById(assetId);
    }

    // ✅ Fallback
    public AssetDTO assetFallback(
            Long assetId,
            Exception e) {
        System.out.println(
                "⚠️ Asset service DOWN! " +
                        "Fallback for: " + assetId);
        AssetDTO fallback = new AssetDTO();
        fallback.setAssetId(assetId);
        fallback.setType("UNKNOWN");
        fallback.setStatus("SERVICE_UNAVAILABLE");
        return fallback;
    }

    // ✅ Create Outage
    public Outage createOutage(OutageDTO dto) {

        if (dto.getAffectedAssets() == null
                || dto.getAffectedAssets()
                .isEmpty()) {
            throw new BadRequestException(
                    "Affected assets cannot be empty");
        }

        if (dto.getSeverity() == null
                || dto.getSeverity().isBlank()) {
            throw new BadRequestException(
                    "Severity is required");
        }

        List<String> validSeverity =
                List.of("LOW", "MEDIUM",
                        "HIGH", "CRITICAL");

        if (!validSeverity.contains(
                dto.getSeverity().toUpperCase())) {
            throw new BadRequestException(
                    "Invalid Severity value");
        }

        if (dto.getReportedBy() == null) {
            throw new BadRequestException(
                    "ReportedBy is required");
        }

        Outage outage = new Outage();

        ObjectMapper mapper = new ObjectMapper();
        try {
            outage.setAffectedAssetsJSON(
                    mapper.writeValueAsString(
                            dto.getAffectedAssets()));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error converting affectedAssets");
        }

        outage.setSeverity(dto.getSeverity());
        outage.setReportedBy(dto.getReportedBy());
        outage.setReportedAt(Instant.now());
        outage.setStatus("OPEN");

        Outage saved = outageRepository.save(outage);

        // Notify the reporter; treat HIGH/CRITICAL outages as ALERTs.
        String sev = saved.getSeverity() == null ? "MEDIUM"
                : saved.getSeverity().toUpperCase();
        String type = sev.equals("HIGH") || sev.equals("CRITICAL")
                ? "ALERT" : "WARNING";
        notifications.publish(
                saved.getReportedBy(),
                saved.getId(),
                "outage",
                "OUTAGE",
                type,
                sev,
                "Outage reported (" + sev + ")",
                "Outage #" + saved.getId() + " is "
                        + saved.getStatus() + "."
        );

        audit.log(saved.getReportedBy(), null, "CREATE", "Outage", saved.getId(),
                "severity=" + saved.getSeverity()
                        + ", assets=" + saved.getAffectedAssetsJSON());

        return saved;
    }

    // ✅ Get All Outages
    public List<Outage> findAll() {
        return outageRepository.findAll();
    }

    public Outage findById(Long id) {
        return outageRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Outage not found: " + id));
    }

    /**
     * Partial update of status / severity / notes (description). When status
     * transitions to RESOLVED, fire a notification to the original reporter.
     * Body keys recognised: status, severity, notes (or description).
     */
    public Outage patch(Long id, Map<String, Object> body) {
        Outage outage = findById(id);
        String prevStatus = outage.getStatus();

        Object status = body.get("status");
        if (status instanceof String s && !s.isBlank()) {
            String newStatus = s.toUpperCase();
            outage.setStatus(newStatus);
            /* Stamp the resolution moment exactly once. Flipping out of
             * RESOLVED (e.g. reopening) clears it so a future re-resolution
             * captures a fresh timestamp; this is what Reports MTTR uses. */
            if ("RESOLVED".equals(newStatus)
                    && !"RESOLVED".equalsIgnoreCase(prevStatus)) {
                outage.setResolvedAt(Instant.now());
            } else if (!"RESOLVED".equals(newStatus)
                    && "RESOLVED".equalsIgnoreCase(prevStatus)) {
                outage.setResolvedAt(null);
            }
        }
        Object severity = body.get("severity");
        if (severity instanceof String s && !s.isBlank()) {
            outage.setSeverity(s.toUpperCase());
        }
        // Outage entity has no `notes` field; the existing schema stores
        // affectedAssetsJSON only. We swallow notes silently for now to keep
        // the API forward-compatible if the column is added later.

        Outage saved = outageRepository.save(outage);

        if ("RESOLVED".equalsIgnoreCase(saved.getStatus())
                && !"RESOLVED".equalsIgnoreCase(prevStatus)) {
            notifications.publish(
                    saved.getReportedBy(),
                    saved.getId(),
                    "outage",
                    "OUTAGE",
                    "SUCCESS",
                    "LOW",
                    "Outage #" + saved.getId() + " resolved",
                    "The outage you reported has been marked resolved."
            );
        }

        audit.log(saved.getReportedBy(), null, "UPDATE", "Outage", saved.getId(),
                "from=" + prevStatus + " to=" + saved.getStatus()
                        + ", severity=" + saved.getSeverity());

        return saved;
    }

    /**
     * Cascade-delete via JPQL.
     *
     * `incident_tasks` references `outages` via a plain `outage_id` column
     * (no JPA relation, but a DB FK may exist). We wipe dependent rows first
     * and then run a JPQL DELETE on the outage so Hibernate's cascade and
     * dirty-checking can't keep the row alive. The repository's
     * `clearAutomatically + flushAutomatically` ensures any cached state is
     * dropped before the controller returns.
     */
    @Transactional
    public void delete(Long id) {
        if (!outageRepository.existsById(id)) {
            throw new BadRequestException("Outage not found: " + id);
        }
        incidentTaskRepository.deleteByOutageId(id);
        int rows = outageRepository.hardDeleteById(id);
        if (rows == 0) {
            throw new BadRequestException("Outage not found: " + id);
        }
        audit.log(null, null, "DELETE", "Outage", id, "cascade incident tasks");
    }
}