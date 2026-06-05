package com.cts.identity_service.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponseDTO {
    private String message;
    private long userID;
    private String username;
    private String name;
    private String email;
    private String role;
    private String status;
}
