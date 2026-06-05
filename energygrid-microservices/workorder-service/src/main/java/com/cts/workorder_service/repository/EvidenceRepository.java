package com.cts.workorder_service.repository;

import com.cts.workorder_service.entity.MaintenanceEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EvidenceRepository extends JpaRepository<MaintenanceEvidence, Long> {

    /**
     * Hard delete via JPQL. Bypasses Hibernate's cascade / orphan-removal /
     * dirty-checking on the parent WorkOrder.evidences collection so the
     * row is actually removed even when EAGER-fetched collections are in
     * play. `clearAutomatically` + `flushAutomatically` make sure the
     * persistence context has no stale copies after the call.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM MaintenanceEvidence e WHERE e.id = :id")
    int hardDeleteById(Long id);
}
