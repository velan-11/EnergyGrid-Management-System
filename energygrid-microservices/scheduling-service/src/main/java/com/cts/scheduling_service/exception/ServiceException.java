package com.cts.scheduling_service.exception;

public class ServiceException extends RuntimeException {
    private String errorCode;

    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
    }

    public ServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

