package com.cts.workorder_service.exception;

public class TechnicianNotFoundException extends RuntimeException{
    public TechnicianNotFoundException(Long id){
        super("Technician not found with id: "+id);

    }}
