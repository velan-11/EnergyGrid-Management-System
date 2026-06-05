package com.cts.workorder_service.exception;

public class WorkOrderNotFoundException extends RuntimeException{
    public WorkOrderNotFoundException(Long id)  {
        super("WorkOrder not found with id: "+id);
    }
}
