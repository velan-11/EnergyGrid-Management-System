package com.cts.identity_service.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForgetUsernameResponseDTO {

    private String username;
    private String message;
}