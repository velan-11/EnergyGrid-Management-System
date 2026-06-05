package com.cts.outage_service.service;

import com.cts.outage_service.client
        .AssetServiceClient;
import com.cts.outage_service.client
        .NotificationServiceClient;
import com.cts.outage_service.dto.RequestDTO.AssetDTO;
import com.cts.outage_service.dto.RequestDTO.NotificationDTO;
import com.cts.outage_service.dto.RequestDTO.IncidentTaskDTO;
import com.cts.outage_service.entity.IncidentTask;
import com.cts.outage_service.entity.Outage;
import com.cts.outage_service.exception
        .BadRequestException;
import com.cts.outage_service.exception
        .ResourceNotFoundException;
import com.cts.outage_service.mapper
        .IncidentTaskMapper;
import com.cts.outage_service.repository
        .IncidentTaskRepository;
import com.cts.outage_service.repository
        .OutageRepository;
import io.github.resilience4j.circuitbreaker
        .annotation.CircuitBreaker;
import io.github.resilience4j.retry
        .annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentTaskService {

    private final IncidentTaskRepository
            incidentTaskRepository;
    private final OutageRepository
            outageRepository;
    private final NotificationServiceClient
            notificationServiceClient;
    private final AssetServiceClient
            assetServiceClient;

    // ✅ Circuit Breaker for Notification
    @CircuitBreaker(
            name = "notificationService",
            fallbackMethod = "notificationFallback"
    )
    @Retry(name = "notificationService")
    public void sendNotification(
            Long userId,
            Long entityId,
            String message,
            String category,
            String severity) {

        NotificationDTO dto = new NotificationDTO(
                userId,
                entityId,
                message,
                category,
                severity
        );

        notificationServiceClient
                .createNotification(dto);

        System.out.println(
                "✅ Notification sent to user: "
                        + userId);
    }

    // ✅ Fallback for Notification
    public void notificationFallback(
            Long userId,
            Long entityId,
            String message,
            String category,
            String severity,
            Exception e) {
        System.out.println(
                "⚠️ Notification service DOWN! " +
                        "Could not notify user: " + userId +
                        " Message: " + message);
    }

    // ✅ Circuit Breaker for Asset
    @CircuitBreaker(
            name = "assetService",
            fallbackMethod = "assetFallback"
    )
    @Retry(name = "assetService")
    public AssetDTO getAssetDetails(
            Long assetId) {
        return assetServiceClient
                .getAssetById(assetId);
    }

    // ✅ Fallback for Asset
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

    // ✅ CREATE TASK
    public IncidentTask createIncidentTask(
            IncidentTaskDTO dto) {

        if (dto.getOutageId() == null) {
            throw new BadRequestException(
                    "OutageId is required");
        }

        if (dto.getAssignedTo() == null) {
            throw new BadRequestException(
                    "Assigned user is required");
        }

        // Validate outage exists
        Outage outage = outageRepository
                .findById(dto.getOutageId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Outage not found"));

        IncidentTask task =
                IncidentTaskMapper.toEntity(dto);
        task.setOutageId(dto.getOutageId());
        task.setAssignedTo(dto.getAssignedTo());
        task.setAssignedAt(Instant.now());
        task.setStatus("ASSIGNED");

        // Save task
        IncidentTask savedTask =
                incidentTaskRepository.save(task);

        // ✅ Send notification via Feign
        sendNotification(
                savedTask.getAssignedTo(),
                savedTask.getId(),
                "Task assigned for outage: "
                        + outage.getSeverity(),
                "TASK",
                "MEDIUM"
        );

        return savedTask;
    }

    // ✅ GET BY ID
    public IncidentTask getById(Long id) {
        return incidentTaskRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found"));
    }

    // ✅ GET ALL
    public List<IncidentTask> getAll() {
        return incidentTaskRepository.findAll();
    }

    // ✅ UPDATE STATUS
    public IncidentTask updateStatus(
            Long id, String status) {

        IncidentTask task =
                incidentTaskRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"));

        List<String> validStatus = List.of(
                "ASSIGNED",
                "IN_PROGRESS",
                "COMPLETED"
        );

        if (status == null || !validStatus
                .contains(status.toUpperCase())) {
            throw new BadRequestException(
                    "Invalid status value");
        }

        task.setStatus(status.toUpperCase());

        // ✅ If completed → notify
        if (status.equalsIgnoreCase("COMPLETED")) {
            task.setCompletedAt(Instant.now());

            sendNotification(
                    task.getAssignedTo(),
                    task.getId(),
                    "Task completed successfully",
                    "TASK",
                    "LOW"
            );
        }

        IncidentTask saved = incidentTaskRepository.save(task);

        // Propagate completion to the parent outage. Previously the
        // outage stayed OPEN forever even after the technician finished
        // the work — operators had to manually flip it, which was easy
        // to forget. Auto-resolve only when EVERY task on the outage
        // is COMPLETED, so partial progress doesn't prematurely close
        // the outage.
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus()) && saved.getOutageId() != null) {
            maybeResolveOutage(saved.getOutageId());
        }

        return saved;
    }

    /**
     * If every incident task for the given outage is COMPLETED, mark
     * the outage RESOLVED. No-op when the outage is already
     * RESOLVED/CLOSED or when any sibling task is still open.
     */
    private void maybeResolveOutage(Long outageId) {
        List<IncidentTask> siblings = incidentTaskRepository.findByOutageId(outageId);
        if (siblings.isEmpty()) return;

        boolean allDone = siblings.stream()
                .allMatch(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())
                        || "DONE".equalsIgnoreCase(t.getStatus()));
        if (!allDone) return;

        outageRepository.findById(outageId).ifPresent(outage -> {
            String cur = outage.getStatus();
            if (cur != null && ("RESOLVED".equalsIgnoreCase(cur) || "CLOSED".equalsIgnoreCase(cur))) {
                return;
            }
            outage.setStatus("RESOLVED");
            // Stamp the resolution moment so Reports MTTR can compute
            // resolvedAt − reportedAt instead of (now − reportedAt).
            outage.setResolvedAt(java.time.Instant.now());
            outageRepository.save(outage);
        });
    }

    // ✅ UPDATE EVIDENCE
    public IncidentTask updateEvidence(
            Long id, String evidenceURI) {

        IncidentTask task =
                incidentTaskRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"));

        task.setEvidenceURI(evidenceURI);
        return incidentTaskRepository.save(task);
    }
}