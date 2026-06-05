//package com.cts.identity_service.service;
//import com.cts.identity_service.dto.RequestDTO.*;
//import com.cts.identity_service.dto.ResponseDTO.ForgetUsernameResponseDTO;
//import com.cts.identity_service.dto.ResponseDTO.LoginResponseDTO;
//import com.cts.identity_service.dto.ResponseDTO.RegisterResponseDTO;
//import com.cts.identity_service.repository.PasswordResetTokenRepository;
//import com.cts.identity_service.entity.PasswordResetToken;
//import com.cts.identity_service.entity.User;
//import com.cts.identity_service.exception.UserNotFoundException;
//import com.cts.identity_service.repository.UserRepository;
//import com.cts.identity_service.security.JWTUtil;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final AuditService auditService;
//    private final JWTUtil jwtUtil;
//    private final PasswordResetTokenRepository passwordResetTokenRepository;
//    private final EmailService emailService;
//
//    public LoginResponseDTO login(@Valid LoginDTO dto) {
//
//        // 1. Find user by email
//        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> {
//            throw new UserNotFoundException("Invalid Email or Password");
//        });
//
//
//        if (user.isDeleted()) {
//            throw new RuntimeException("User account is deleted. Please contact admin.");
//        }
//
//
//        // 2. Check account status
//        if (user.getStatus() != User.Status.ACTIVE) {
//
//            // AUDIT: login attempted on inactive/blocked account
//            auditService.createAuditLog(
//                    user,
//                    "LOGIN_FAILED",
//                    "AUTH",
//                    "Login failed: account is " + user.getStatus().name()
//            );
//
//            throw new RuntimeException(
//                    "Account is " + user.getStatus().name().toLowerCase() + ". Please contact admin.");
//        }
//
//        // 3. Verify password
//        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
//
//            // AUDIT: wrong password
//            auditService.createAuditLog(
//                    user,
//                    "LOGIN_FAILED",
//                    "AUTH",
//                    "Login failed: invalid password"
//            );
//
//            throw new UserNotFoundException("Invalid email or password");
//        }
//
//        // 4. Generate JWT (LOGIN SUCCESS)
//        String token = jwtUtil.generateToken(
//                user.getEmail(),
//                user.getRole().name()
//        );
//
//        // AUDIT: successful login
//        auditService.createAuditLog(
//                user,
//                "LOGIN",
//                "AUTH",
//                "User logged in successfully"
//        );
//
//        return new LoginResponseDTO(
//                token,
//                user.getUserId(),
//                user.getUsername(),
//                user.getName(),
//                user.getRole().name(),
//                user.getStatus().name()
//        );
//    }
//
//    // Register
//    public RegisterResponseDTO register(@Valid RegisterDTO dto) {
//
//        // 1. Check if email already exists
//        if (userRepository.existsByEmail(dto.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//        // 2. Build and save user
//        User user = User.builder()
//                .name(dto.getName())
//                .username(dto.getUsername())
//                .email(dto.getEmail())
//                .phone((dto.getPhone()))
//                .password(passwordEncoder.encode((dto.getPassword())))
//                .role(User.Role.valueOf(dto.getRole()))
//                .status(User.Status.PENDING)
//                .build();
//        User savedUser = userRepository.save(user);
//
//        // 3. Return response
//        return new RegisterResponseDTO(
//                "Registration successful. You account is pending admin approval",
//                savedUser.getUserId(),
//                savedUser.getUsername(),
//                savedUser.getName(),
//                savedUser.getEmail(),
//                savedUser.getRole().name(),
//                savedUser.getStatus().name()
//        );
//
//    }
//
//    public String forgetPassword(ForgetPasswordDTO dto) {
//        User user = userRepository.findByEmail(dto.getEmail())
//                .orElseThrow(() -> new RuntimeException("Email not found."));
//
//        passwordResetTokenRepository.deleteByUserID(user.getUserId());
//
//        String rawToken = generateSixDigitOtp();
//
//        PasswordResetToken resetToken = PasswordResetToken.builder()
//                .userID(user.getUserId())
//                .token(rawToken)
//                .expiresAt(LocalDateTime.now().plusMinutes(1))
//                .used(false)
//                .build();
//
//        passwordResetTokenRepository.save(resetToken);
//
//        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
//
//        return "A reset token has been sent to your email.";
//    }
//
//    private String generateSixDigitOtp() {
//        return String.valueOf((int) (Math.random() * 900000) + 100000);
//    }
//
//
//    // Reset password
//
//    public String resetPassword(ResetPasswordDTO dto) {
//
//        // 1. find token
//        PasswordResetToken resetToken = passwordResetTokenRepository
//                .findByToken(dto.getToken())
//                .orElseThrow(() -> new RuntimeException("Invalid OTP."));
//
//        // 2. check used
//        if(resetToken.isUsed()){
//            throw new RuntimeException("OTP already used.");
//        }
//
//        // 3. check expired
//        if(LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
//            throw new RuntimeException("OTP expired");
//        }
//
//        // 4. update password
//        User user = userRepository.findByUserId(resetToken.getUserID())
//                .orElseThrow(() -> new RuntimeException("User not found."));
//
//        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
//        userRepository.save(user);
//
//        // 5. mark token used
//        resetToken.setUsed(true);
//        passwordResetTokenRepository.save(resetToken);
//
//        return "Password reset successful. You can login now.";
//    }
//
//
//    public ForgetUsernameResponseDTO forgetUsername(
//            @Valid ForgetUsernameDTO dto) {
//
//        User user = userRepository.findByEmail(dto.getEmail())
//                .orElseThrow(() ->
//                        new RuntimeException("User not found"));
//
//        return new ForgetUsernameResponseDTO(
//                user.getUsername(),
//                "Your username has been sent to your email"
//        );
//    }
//
//
//}



package com.cts.identity_service.service;


import com.cts.identity_service.dto.RequestDTO.*;
import com.cts.identity_service.dto.ResponseDTO.ForgetUsernameResponseDTO;
import com.cts.identity_service.dto.ResponseDTO.LoginResponseDTO;
import com.cts.identity_service.dto.ResponseDTO.RegisterResponseDTO;
import com.cts.identity_service.repository.PasswordResetTokenRepository;
import com.cts.identity_service.service.AuditService;
import com.cts.identity_service.entity.PasswordResetToken;
import com.cts.identity_service.entity.User;
import com.cts.identity_service.exception.UserNotFoundException;
import com.cts.identity_service.repository.UserRepository;
import com.cts.identity_service.security.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final JWTUtil jwtUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public LoginResponseDTO login(@Valid LoginDTO dto) {

        // 1. Find user by email
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    // LOGIN FAILED (email not found â†’ no user to link)
                    throw new UserNotFoundException("Invalid Email or Password");
                });


        if (user.isDeleted()) {
            throw new RuntimeException("User account is deleted. Please contact admin.");
        }


        // 2. Check account status
        if (user.getStatus() != User.Status.ACTIVE) {

            // AUDIT: login attempted on inactive/blocked account
            auditService.createAuditLog(
                    user,
                    "LOGIN_FAILED",
                    "AUTH",

                    "Login failed: account is " + user.getStatus().name()
            );

            throw new RuntimeException(
                    "Account is " + user.getStatus().name().toLowerCase() +
                            ". Please contact admin."
            );
        }

        // 3. Verify password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {

            // AUDIT: wrong password
            auditService.createAuditLog(
                    user,
                    "LOGIN_FAILED",
                    "AUTH",

                    "Login failed: invalid password"
            );

            throw new UserNotFoundException("Invalid email or password");
        }

        // 4. Generate JWT (LOGIN SUCCESS)
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // AUDIT: successful login
        auditService.createAuditLog(
                user,
                "LOGIN",
                "AUTH",

                "User logged in successfully"
        );

        return new LoginResponseDTO(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }

    // Register
    public RegisterResponseDTO register(@Valid RegisterDTO dto) {

        // 1a. Email already taken?
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("That email is already registered.");
        }

        // 1b. Username already taken? (DB enforces this too via @Column(unique)
        //     but checking up-front lets us return a friendly message instead
        //     of a generic data-integrity error.)
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("That username is already taken. Pick another.");
        }

        // 1c. Validate the role enum value before reflective lookup so a typo
        //     surfaces as a clean BAD_REQUEST instead of IllegalArgumentException.
        try {
            User.Role.valueOf(dto.getRole());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Invalid role '" + dto.getRole() + "'. " +
                            "Allowed: ADMIN, OPERATOR, TECHNICIAN, PRODUCER, CUSTOMER, AUDITOR.");
        }

        // 2. Build and save user — ADMIN gets ACTIVE immediately, others stay PENDING
        User.Status status = User.Role.ADMIN.name().equals(dto.getRole())
                ? User.Status.ACTIVE
                : User.Status.PENDING;

        User user = User.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phone((dto.getPhone()))
                .password(passwordEncoder.encode((dto.getPassword())))
                .role(User.Role.valueOf(dto.getRole()))
                .status(status)
                .build();



        User savedUser = userRepository.save(user);

        // 3. Return response
        String message = User.Role.ADMIN.name().equals(dto.getRole())
                ? "Registration successful. You can log in now."
                : "Registration successful. Your account is pending admin approval.";
        return new RegisterResponseDTO(
                message,
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getStatus().name()
        );

    }

    // Forget Password
    public String forgetPassword(ForgetPasswordDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new RuntimeException("Email not found."));

        passwordResetTokenRepository
                .deleteByUserID(user.getUserId());

        String rawToken = String.valueOf(
                (int)(Math.random() * 900000) + 100000
        );
        logger.info("The OTP for reset password is : {}",rawToken);
        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .userID(user.getUserId())
                        .token(rawToken)
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .used(false)
                        .build();

        passwordResetTokenRepository.save(resetToken);
        return "OTP sent successfully";
    }



    // Reset password

    public String resetPassword(ResetPasswordDTO dto) {

        // 1. find token
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid OTP."));

        // 2. check used
        if(resetToken.isUsed()){
            throw new RuntimeException("OTP already used.");
        }

        // 3. check expired
        if(LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("OTP expired");
        }

        // 4. update password
        User user = userRepository.findByUserId(resetToken.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // 5. mark token used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return "Password reset successful. You can login now.";
    }


    public ForgetUsernameResponseDTO forgetUsername(
            @Valid ForgetUsernameDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
                new RuntimeException("User not found"));

        return new ForgetUsernameResponseDTO(user.getUsername(),
                "Your username has been sent to your email"
        );
    }


}
