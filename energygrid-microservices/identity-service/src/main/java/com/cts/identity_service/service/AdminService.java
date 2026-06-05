package com.cts.identity_service.service;
import com.cts.identity_service.dto.ResponseDTO.AdminUserResponseDTO;
import com.cts.identity_service.entity.User;
import com.cts.identity_service.exception.UserNotFoundException;
import com.cts.identity_service.mapper.UserMapper;
import com.cts.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<AdminUserResponseDTO> getPendingUsers(){
        List<AdminUserResponseDTO> dto = userRepository.findByStatus(User.Status.PENDING).stream()
                .map(UserMapper::toDTO).toList();
        return dto;
    }

    public List<AdminUserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    public String approveUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
        return "User approved successfully";
    }

    public String deleteUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);
        auditService.createAuditLog(
                user,
                "DELETE",
                "User",
                "Admin soft-deleted user"
        );
        return "User with ID " + userId + " has been deleted successfully";
    }

    public String restoreUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
        return "User with ID " + userId + " has been restored successfully";
    }

    public ResponseEntity<AdminUserResponseDTO> getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .map(user -> ResponseEntity.ok(UserMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
