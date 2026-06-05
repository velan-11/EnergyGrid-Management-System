package com.cts.identity_service.controller;
import com.cts.identity_service.dto.ResponseDTO.ForgetUsernameResponseDTO;
import com.cts.identity_service.dto.ResponseDTO.LoginResponseDTO;
import com.cts.identity_service.dto.ResponseDTO.RegisterResponseDTO;
import com.cts.identity_service.dto.RequestDTO.*;
import com.cts.identity_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    // Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // Register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    // Forget-password
    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(
            @RequestBody ForgetPasswordDTO dto) {
        return ResponseEntity.ok(authService.forgetPassword(dto));
    }

    // Reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordDTO dto) {
        return ResponseEntity.ok(authService.resetPassword(dto));
    }

    @PostMapping("/forget-username")
    public ResponseEntity<ForgetUsernameResponseDTO> forgetUsername(
            @Valid @RequestBody ForgetUsernameDTO dto) {

        return ResponseEntity.ok(authService.forgetUsername(dto));
    }
    
}

