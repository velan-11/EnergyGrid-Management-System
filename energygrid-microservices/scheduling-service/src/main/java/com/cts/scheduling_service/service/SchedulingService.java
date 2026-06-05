package com.cts.scheduling_service.service;

import com.cts.scheduling_service.client.AssetServiceClient;
import com.cts.scheduling_service.dto.RequestDTO.AssetDTO;
import com.cts.scheduling_service.dto.RequestDTO.ScheduleRequestDto;
import com.cts.scheduling_service.entity.GenerationSchedule;
import com.cts.scheduling_service.entity.ScheduleStatus;
import com.cts.scheduling_service.exception.ExternalServiceException;
import com.cts.scheduling_service.exception.InvalidDataException;
import com.cts.scheduling_service.exception.ResourceNotFoundException;
import com.cts.scheduling_service.notification.NotificationPublisher;
import com.cts.scheduling_service.repository.GenerationScheduleRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages generation schedules: validates input, confirms the asset exists via
 * a resilient Feign call (circuit breaker + retry), and handles create / query /
 * cancel, emitting best-effort notifications and audit entries along the way.
 */
@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final GenerationScheduleRepository repository;
    private final AssetServiceClient assetServiceClient;
    private final NotificationPublisher notifications;
    private final AuditService audit;

    // Resilient asset lookup: trips to assetFallback on repeated failures.
    @CircuitBreaker(
            name = "assetService",
            fallbackMethod = "assetFallback"
    )
    @Retry(name = "assetService")
    public AssetDTO getAssetDetails(Long assetId) {
        if (assetId == null || assetId <= 0) {
            throw new InvalidDataException("assetId", "Must be a positive number");
        }
        return assetServiceClient.getAssetById(assetId);
    }

    // Fallback when asset-service is unavailable — surfaces a 502/503 to the caller.
    public AssetDTO assetFallback(Long assetId, Exception e) {
        throw new ExternalServiceException(
                "asset-service",
                "Asset service is unavailable. " + e.getMessage()
        );
    }

    public GenerationSchedule createSchedule(ScheduleRequestDto dto) {
        // Validate input data
        validateScheduleRequestDto(dto);

        // Verify asset exists via Feign call with Circuit Breaker
        getAssetDetails(dto.getAssetId());

        // Create and save schedule
        GenerationSchedule schedule = new GenerationSchedule();
        schedule.setAssetId(dto.getAssetId());
        schedule.setStartAt(dto.getStartAt());
        schedule.setEndAt(dto.getEndAt());
        schedule.setTargetKw(dto.getTargetKw());
        schedule.setCreatedBy(dto.getCreatedBy());

        GenerationSchedule saved = repository.save(schedule);

        // Best-effort notification — never blocks the create
        Long actor = parseUserId(dto.getCreatedBy());
        if (actor != null) {
            notifications.publish(
                    actor,
                    saved.getScheduleId(),
                    "schedule",
                    "SCHEDULE",
                    "INFO",
                    "LOW",
                    "Schedule #" + saved.getScheduleId() + " created",
                    "Generation schedule for asset #" + saved.getAssetId()
                            + " has been created."
            );
        }
        audit.log(actor, dto.getCreatedBy(), "CREATE", "Schedule", saved.getScheduleId(),
                "asset=" + saved.getAssetId() + ", targetKw=" + saved.getTargetKw()
                        + ", from=" + saved.getStartAt() + " to=" + saved.getEndAt());
        return saved;
    }

    private static Long parseUserId(String s) {
        if (s == null) return null;
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    public List<GenerationSchedule> getAllSchedules() {
        return repository.findAll();
    }

    public GenerationSchedule getScheduleById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("id", "Must be a positive number");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GenerationSchedule", "scheduleId", id));
    }

    public List<GenerationSchedule> getSchedulesByAsset(Long assetId) {
        if (assetId == null || assetId <= 0) {
            throw new InvalidDataException("assetId", "Must be a positive number");
        }
        return repository.findByAssetId(assetId);
    }

    public GenerationSchedule cancelSchedule(Long id) {
        GenerationSchedule schedule = getScheduleById(id);

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new InvalidDataException("status", "Schedule is already cancelled");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        GenerationSchedule saved = repository.save(schedule);

        Long actor = parseUserId(saved.getCreatedBy());
        if (actor != null) {
            notifications.publish(
                    actor,
                    saved.getScheduleId(),
                    "schedule",
                    "SCHEDULE",
                    "WARNING",
                    "MEDIUM",
                    "Schedule #" + saved.getScheduleId() + " cancelled",
                    "Your generation schedule has been cancelled."
            );
        }
        audit.log(actor, saved.getCreatedBy(), "CANCEL", "Schedule",
                saved.getScheduleId(), "status=CANCELLED");
        return saved;
    }

    private void validateScheduleRequestDto(ScheduleRequestDto dto) {
        if (dto.getAssetId() == null || dto.getAssetId() <= 0) {
            throw new InvalidDataException("assetId", "Must be a positive number");
        }
        if (dto.getStartAt() == null) {
            throw new InvalidDataException("startAt", "Start time is required");
        }
        if (dto.getEndAt() == null) {
            throw new InvalidDataException("endAt", "End time is required");
        }
        if (dto.getStartAt().isAfter(dto.getEndAt())) {
            throw new InvalidDataException("schedule", "Start time must be before end time");
        }
        if (dto.getTargetKw() == null || dto.getTargetKw().signum() <= 0) {
            throw new InvalidDataException("targetKw", "Must be greater than 0");
        }
        if (dto.getCreatedBy() == null || dto.getCreatedBy().trim().isEmpty()) {
            throw new InvalidDataException("createdBy", "Creator information is required");
        }
    }
}