package com.cts.workorder_service.exception;

public class EvidenceNotFoundException extends RuntimeException {
    public EvidenceNotFoundException(Long id) {
        super("Evidence not found with id: " + id);
    }
}