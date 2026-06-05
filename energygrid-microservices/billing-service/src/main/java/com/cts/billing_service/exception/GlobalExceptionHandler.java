package com.cts.billing_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind
        .MethodArgumentNotValidException;
import org.springframework.web.bind.annotation
        .ExceptionHandler;
import org.springframework.web.bind.annotation
        .RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(
            BillingNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleBillingNotFound(
            BillingNotFoundException ex) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "BILLING_NOT_FOUND"
        );
    }


    @ExceptionHandler(
            InvoiceNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleInvoiceNotFound(
            InvoiceNotFoundException ex) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "INVOICE_NOT_FOUND"
        );
    }


    @ExceptionHandler(
            PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handlePaymentNotFound(
            PaymentNotFoundException ex) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "PAYMENT_NOT_FOUND"
        );
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>>
    handleBadRequest(
            BadRequestException ex) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                "BAD_REQUEST"
        );
    }


    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors =
                new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now().toString());
        body.put("status",
                HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_FAILED");
        body.put("messages", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>>
    handleRuntime(RuntimeException ex) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                "RUNTIME_ERROR"
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneric(Exception ex) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong: "
                        + ex.getMessage(),
                "INTERNAL_ERROR"
        );
    }


    private ResponseEntity<Map<String, Object>>
    buildError(
            HttpStatus status,
            String message,
            String error) {

        Map<String, Object> body =
                new HashMap<>();
        body.put("timestamp",
                LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}