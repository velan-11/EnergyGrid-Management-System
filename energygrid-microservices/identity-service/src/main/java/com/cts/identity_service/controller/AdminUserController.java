package com.cts.identity_service.controller;
import com.cts.identity_service.service.AdminService;
import com.cts.identity_service.dto.ResponseDTO.AdminUserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminService adminService;

    @GetMapping("/pending")
    public List<AdminUserResponseDTO> getPendingUsers() {
        return adminService.getPendingUsers();
    }

    @GetMapping
    public List<AdminUserResponseDTO> getAllUsers() {
        return adminService.getAllUsers();
    }
    @PutMapping("/{userId}/approve")
    public String approveUser(@PathVariable Long userId) {
        return adminService.approveUser(userId);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        return adminService.deleteUser(userId);
    }

    @PutMapping("/{userId}/restore")
    public String restoreUser(@PathVariable Long userId) {
        return adminService.restoreUser(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }
}

