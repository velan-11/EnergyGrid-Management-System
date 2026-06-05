package com.cts.scheduling_service.service;

import com.cts.scheduling_service.entity.AuditLog;
import com.cts.scheduling_service.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * One-call audit-log writer plus read access for the admin audit page.
 * Writes are best-effort — failures are swallowed so auditing never breaks
 * a successful business operation.
 */
@Service
public class AuditService {

    private static final String SERVICE_NAME = "scheduling-service";

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public void log(Long userId, String userName,
                    String action, String resourceType, Long resourceId, String details) {
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

    public Page<AuditLog> list(int page, int size) {
        return repo.findAllByOrderByTimestampDesc(pageable(page, size));
    }

    public Page<AuditLog> listByUser(Long userId, int page, int size) {
        return repo.findByUserIdOrderByTimestampDesc(userId, pageable(page, size));
    }

    public Page<AuditLog> listByResource(String resourceType, int page, int size) {
        return repo.findByResourceTypeOrderByTimestampDesc(resourceType, pageable(page, size));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
    }
}
