package com.cts.billing_service.mapper;

import com.cts.billing_service.dto.RequestDTO.InvoiceRequestDTO;
import com.cts.billing_service.entity.Invoice;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    private final ModelMapper modelMapper;

    public InvoiceMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Invoice toEntity(InvoiceRequestDTO dto) {
        return modelMapper.map(dto, Invoice.class);
    }
}
