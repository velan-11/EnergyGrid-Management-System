package com.cts.workorder_service.repository;

import com.cts.workorder_service.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicianRepository extends JpaRepository<Technician,Long> {
}


