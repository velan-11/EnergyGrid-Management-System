package com.cts.billing_service.repository;

import com.cts.billing_service.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findByCustomerId(Long customerId);
}
