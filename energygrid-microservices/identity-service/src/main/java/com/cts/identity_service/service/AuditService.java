package com.cts.identity_service.service;
import com.cts.identity_service.entity.AuditLog;
import com.cts.identity_service.entity.User;
import com.cts.identity_service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void createAuditLog(User user,String action,String resourceType, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUser_UserId(userId);
    }
}