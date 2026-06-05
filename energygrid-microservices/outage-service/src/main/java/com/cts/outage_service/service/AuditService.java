package com.cts.outage_service.service;

import com.cts.outage_service.entity.AuditLog;
import com.cts.outage_service.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final String SERVICE_NAME = "outage-service";
    private final AuditLogRepository repo;
    public AuditService(AuditLogRepository repo) { this.repo = repo; }

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
        } catch (RuntimeException ignored) { /* best-effort */ }
    }


}
