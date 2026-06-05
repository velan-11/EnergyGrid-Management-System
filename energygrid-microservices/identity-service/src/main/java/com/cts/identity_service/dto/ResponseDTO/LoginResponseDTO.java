package com.cts.identity_service.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private long userId;
    private String username;
    private String name;
    private String role;
    private String status;

}
