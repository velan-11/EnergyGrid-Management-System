package com.cts.identity_service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    // Before any service method runs
    @Before("execution(* com.cts.identity_service.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println(" Entering: " + joinPoint.getSignature().getName());
    }

    //  After method completes
    @AfterReturning("execution(* com.cts.identity_service.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println(" Completed: " + joinPoint.getSignature().getName());
    }
}