package com.cts.outage_service.repository;

import com.cts.outage_service.entity.Outage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/** Data access for outages. */
public interface OutageRepository extends JpaRepository<Outage, Long> {

    // JPQL delete that bypasses Hibernate cascade/dirty-checking; the
    // clear/flush flags drop cached state so the row can't be resurrected.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Outage o WHERE o.id = :id")
    int hardDeleteById(Long id);
}
