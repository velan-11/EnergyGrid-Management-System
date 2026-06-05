package com.cts.identity_service.dto.RequestDTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordDTO {

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;
}
