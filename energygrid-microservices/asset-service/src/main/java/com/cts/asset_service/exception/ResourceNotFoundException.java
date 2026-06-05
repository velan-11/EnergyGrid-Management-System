package com.cts.asset_service.exception;
/** Thrown when a requested entity does not exist; surfaces as an HTTP 404 to clients. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
