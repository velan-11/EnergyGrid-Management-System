package com.cts.demand_response_service.service;

import com.cts.demand_response_service.entity.AuditLog;
import com.cts.demand_response_service.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final String SERVICE_NAME = "demand-response-service";
    private final AuditLogRepository repo;
    public AuditService(AuditLogRepository repo) { this.repo = repo; }

    public void log(Long userId, String userName,
                    String action, String resourceType, Long resourceId, String details) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(userId); log.setUserName(userName);
            log.setAction(action); log.setResourceType(resourceType);
            log.setResourceId(resourceId); log.setDetails(details);
            log.setServiceName(SERVICE_NAME);
            repo.save(log);
        } catch (RuntimeException ignored) { /* best-effort */ }
    }

    public Page<AuditLog> list(int page, int size) {
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),200));
        return repo.findAllByOrderByTimestampDesc(p);
    }

}
