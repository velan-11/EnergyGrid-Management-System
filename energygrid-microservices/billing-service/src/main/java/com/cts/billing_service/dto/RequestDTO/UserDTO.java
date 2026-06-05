package com.cts.billing_service.dto.RequestDTO;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String status;
}