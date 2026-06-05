package com.cts.outage_service.repository;

import com.cts.outage_service.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);
}
