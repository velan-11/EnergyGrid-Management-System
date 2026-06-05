package com.cts.scheduling_service.exception;

public class ExternalServiceException extends RuntimeException {
    private String serviceName;
    private int statusCode;

    public ExternalServiceException(String serviceName, String message) {
        super("External service error from " + serviceName + ": " + message);
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super("External service error from " + serviceName + " (Status: " + statusCode + "): " + message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

