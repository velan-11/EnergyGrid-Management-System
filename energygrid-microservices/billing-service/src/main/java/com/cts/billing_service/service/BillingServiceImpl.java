package com.cts.billing_service.service;

import com.cts.billing_service.client
        .UserServiceClient;
import com.cts.billing_service.dto.RequestDTO.UserDTO;
import com.cts.billing_service.dto.RequestDTO
        .BillingRequestDTO;
import com.cts.billing_service.dto.ResponseDTO
        .BillingResponseDTO;
import com.cts.billing_service.entity.Billing;
import com.cts.billing_service.mapper
        .BillingMapper;
import com.cts.billing_service.repository
        .BillingRepository;
import io.github.resilience4j.circuitbreaker
        .annotation.CircuitBreaker;
import io.github.resilience4j.retry
        .annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default billing implementation. Enriches each bill with customer data
 * from the identity-service via Feign, guarded by a circuit breaker and retry.
 */
@Service
@RequiredArgsConstructor
public class BillingServiceImpl
        implements BillingService {

    private final BillingRepository repository;
    private final BillingMapper mapper;
    private final UserServiceClient
            userServiceClient;

    // Circuit Breaker + Retry around the identity-service call.
    @CircuitBreaker(
            name = "identityService",
            fallbackMethod = "identityFallback"
    )
    @Retry(name = "identityService")
    public UserDTO getUserDetails(Long userId) {
        System.out.println(
                "Calling identity-service for: "
                        + userId);
        return userServiceClient
                .getUserById(userId);
    }

    // Fallback
    public UserDTO identityFallback(
            Long userId,
            Exception e) {
        System.out.println(
                " Identity service DOWN! " +
                        "Fallback for userId: " + userId +
                        " Error: " + e.getMessage());
        UserDTO fallback = new UserDTO();
        fallback.setUserId(userId);
        fallback.setName("UNKNOWN");
        fallback.setEmail("UNKNOWN");
        fallback.setStatus("SERVICE_UNAVAILABLE");
        return fallback;
    }

    // Create Billing
    @Override
    public BillingResponseDTO createBilling(
            BillingRequestDTO dto) {

        // Feign call with CB + Retry
        UserDTO user = getUserDetails(
                dto.getCustomerId());

        System.out.println(
                "Creating bill for: "
                        + user.getName()
                        + " Status: " + user.getStatus());

        Billing billing = mapper.toEntity(dto);
        billing.setStatus("PENDING");
        billing.setCreatedAt(
                LocalDateTime.now());
        Billing saved = repository.save(billing);
        return mapper.toDTO(saved);
    }

    // Get Billing By Customer
    @Override
    public List<BillingResponseDTO>
    getBillingByCustomer(
            Long customerId) {
        return repository
                .findByCustomerId(customerId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}