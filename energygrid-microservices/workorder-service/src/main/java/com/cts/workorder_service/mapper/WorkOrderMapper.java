package com.cts.workorder_service.mapper;

import com.cts.workorder_service.dto.RequestDTO.WorkOrderRequestDTO;
import com.cts.workorder_service.entity.WorkOrder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/** Maps an inbound {@link WorkOrderRequestDTO} to a {@link WorkOrder} entity via ModelMapper. */
@Component
public class WorkOrderMapper {

    private final ModelMapper modelMapper;

    public WorkOrderMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public WorkOrder toEntity(WorkOrderRequestDTO dto) {
        return modelMapper.map(dto, WorkOrder.class);
    }
}

