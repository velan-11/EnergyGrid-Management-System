package com.cts.demand_response_service.service;

import com.cts.demand_response_service.dto.RequestDTO.ProgramRequestDTO;
import com.cts.demand_response_service.dto.ResponseDTO.ProgramResponseDTO;
import com.cts.demand_response_service.entity.DemandResponseProgram;
import com.cts.demand_response_service.repository.ProgramRepository;
import com.cts.demand_response_service.mapper.ProgramMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final AuditService audit;

    public ProgramService(ProgramRepository programRepository, AuditService audit) {
        this.programRepository = programRepository;
        this.audit = audit;
    }

    public ProgramResponseDTO create(ProgramRequestDTO dto) throws JsonProcessingException {

        // ✅ Get logged-in user email from JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        // ✅ Create entity
        DemandResponseProgram program = new DemandResponseProgram();
        program.setName(dto.name);
        program.setType(dto.type);

        program.setEnrollmentCriteriaJson(
                new ObjectMapper().writeValueAsString(dto.enrollmentCriteriaJson)
        );

        // ✅ Store email instead of User object
        program.setCreatedBy(currentUserEmail);

        DemandResponseProgram saved = programRepository.save(program);
        audit.log(null, currentUserEmail, "CREATE", "DRProgram", saved.getProgramId(),
                "name=" + saved.getName() + ", type=" + saved.getType());
        return ProgramMapper.toResponse(saved);
    }

    public List<ProgramResponseDTO> getAll() {
        return programRepository.findAll()
                .stream()
                .map(ProgramMapper::toResponse)
                .toList();
    }
}