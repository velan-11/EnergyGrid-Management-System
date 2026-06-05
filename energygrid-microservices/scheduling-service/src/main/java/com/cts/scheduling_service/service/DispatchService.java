package com.cts.scheduling_service.service;

import com.cts.scheduling_service.dto.RequestDTO.DispatchRequestDto;
import com.cts.scheduling_service.entity.DispatchRecord;
import com.cts.scheduling_service.entity.DispatchStatus;
import com.cts.scheduling_service.entity.GenerationSchedule;
import com.cts.scheduling_service.entity.ScheduleStatus;
import com.cts.scheduling_service.exception.InvalidDataException;
import com.cts.scheduling_service.exception.ResourceNotFoundException;
import com.cts.scheduling_service.notification.NotificationPublisher;
import com.cts.scheduling_service.repository.DispatchRecordRepository;
import com.cts.scheduling_service.repository.GenerationScheduleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Executes dispatches against a schedule: derives a SUCCESS/PARTIAL/FAILED
 * status from the actual-vs-target ratio, persists the record, flips the
 * schedule to ACTIVE, and fires best-effort notifications plus an audit entry.
 */
@Service
public class DispatchService {

    private final DispatchRecordRepository drRepo;
    private final GenerationScheduleRepository gsRepo;
    private final NotificationPublisher notifications;
    private final AuditService audit;

    public DispatchService(DispatchRecordRepository drRepo,
                           GenerationScheduleRepository gsRepo,
                           NotificationPublisher notifications,
                           AuditService audit) {
        this.drRepo = drRepo;
        this.gsRepo = gsRepo;
        this.notifications = notifications;
        this.audit = audit;
    }

    public DispatchRecord executeDispatch(DispatchRequestDto dto) {
        // 0 is a legal value — it means the dispatch failed, recorded as FAILED.
        // Reject only null and explicit negatives.
        if (dto.getActualKw() == null || dto.getActualKw() < 0) {
            throw new InvalidDataException("actualKw", "Must be 0 or greater");
        }

        GenerationSchedule schedule = gsRepo.findById(dto.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GenerationSchedule", "scheduleId", dto.getScheduleId()));

        BigDecimal target = schedule.getTargetKw();
        BigDecimal actual = BigDecimal.valueOf(dto.getActualKw());

        // Status derived from the actual/target ratio per spec:
        //   actual == 0                          → FAILED
        //   ratio >= 95%                          → SUCCESS
        //   ratio >= 50%                          → PARTIAL
        //   otherwise                             → FAILED
        DispatchStatus status = computeStatus(actual, target);

        DispatchRecord record = new DispatchRecord();
        record.setScheduleId(schedule.getScheduleId());
        record.setExecutedBy(dto.getExecutedBy());
        record.setExecutedByName(dto.getExecutedByName());
        record.setExecutedByUsername(dto.getExecutedByUsername());
        record.setActualKw(actual);
        // Snapshot the target so historic reports don't shift if the schedule
        // is later edited.
        record.setTargetKw(target);
        record.setStatus(status);
        record.setNotes(dto.getNotes());

        schedule.setStatus(ScheduleStatus.ACTIVE);
        gsRepo.save(schedule);

        DispatchRecord saved = drRepo.save(record);

        // Notify only when something needs attention (PARTIAL / FAILED).
        // SUCCESS still gets a low-priority confirmation so the operator has a
        // trace in their inbox.
        Long operator = saved.getExecutedBy();
        if (operator != null) {
            String pct = ratioPercent(actual, target);
            String title;
            String severity;
            String tone;
            switch (status) {
                case SUCCESS -> {
                    title = "Dispatch #" + saved.getDispatchId() + " completed";
                    severity = "LOW";
                    tone = "SUCCESS";
                }
                case PARTIAL -> {
                    title = "Dispatch #" + saved.getDispatchId() + " completed at partial capacity";
                    severity = "MEDIUM";
                    tone = "WARNING";
                }
                default -> { // FAILED, MANUAL_OVERRIDE
                    title = "Dispatch #" + saved.getDispatchId() + " failed";
                    severity = "HIGH";
                    tone = "ERROR";
                }
            }
            String body = "Schedule #" + saved.getScheduleId()
                    + " delivered " + actual.toPlainString()
                    + " kW of " + target.toPlainString() + " kW target"
                    + " (" + pct + ").";
            notifications.publish(
                    operator,
                    saved.getDispatchId(),
                    "dispatch",
                    "DISPATCH",
                    tone,
                    severity,
                    title,
                    body
            );
        }

        audit.log(saved.getExecutedBy(), saved.getExecutedByName(),
                "EXECUTE", "DispatchRecord", saved.getDispatchId(),
                "schedule=" + saved.getScheduleId()
                        + ", actual=" + saved.getActualKw()
                        + ", target=" + target
                        + ", status=" + status);
        return saved;
    }

    /**
     * Percentage-based status calc. Centralised so reports and tests can
     * reuse the exact same rule the write path uses.
     */
    static DispatchStatus computeStatus(BigDecimal actual, BigDecimal target) {
        if (actual == null || actual.compareTo(BigDecimal.ZERO) == 0) {
            return DispatchStatus.FAILED;
        }
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            // Defensive: a zero/negative target is data noise. Mark FAILED so
            // it surfaces in reports rather than silently passing as SUCCESS.
            return DispatchStatus.FAILED;
        }
        BigDecimal pct = actual.multiply(BigDecimal.valueOf(100))
                .divide(target, 2, java.math.RoundingMode.HALF_UP);
        if (pct.compareTo(BigDecimal.valueOf(95)) >= 0) return DispatchStatus.SUCCESS;
        if (pct.compareTo(BigDecimal.valueOf(50)) >= 0) return DispatchStatus.PARTIAL;
        return DispatchStatus.FAILED;
    }

    private static String ratioPercent(BigDecimal actual, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) return "0%";
        BigDecimal pct = actual.multiply(BigDecimal.valueOf(100))
                .divide(target, 1, java.math.RoundingMode.HALF_UP);
        return pct.toPlainString() + "%";
    }

    public List<DispatchRecord> getAllDispatches() {
        return drRepo.findAll();
    }

    public DispatchRecord getDispatchById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("id", "Must be a positive number");
        }
        return drRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DispatchRecord", "dispatchId", id));
    }

    public List<DispatchRecord> getBySchedule(Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new InvalidDataException("scheduleId", "Must be a positive number");
        }
        return drRepo.findByScheduleId(scheduleId);
    }
}
