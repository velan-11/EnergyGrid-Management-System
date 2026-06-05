package com.cts.scheduling_service.service;

import com.cts.scheduling_service.entity.AuditLog;
import com.cts.scheduling_service.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * One-call audit-log writer. Used by the controllers and services in this
 * microservice. Failures are caught and swallowed — auditing must never
 * break a successful business operation; in production this would publish
 * to an out-of-band log sink instead.
 */
@Service
public class AuditService {

    private static final String SERVICE_NAME = "scheduling-service";

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public void log(Long userId, String userName,
                    String action, String resourceType, Long resourceId,
                    String details) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setAction(action);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setDetails(details);
            log.setServiceName(SERVICE_NAME);
            repo.save(log);
        } catch (RuntimeException ignored) {
            // Auditing is best-effort. Never throw out of a write path.
        }
    }


}
