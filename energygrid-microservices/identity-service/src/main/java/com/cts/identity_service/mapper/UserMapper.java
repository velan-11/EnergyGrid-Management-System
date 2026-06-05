package com.cts.identity_service.mapper;
import com.cts.identity_service.dto.RequestDTO.UserSummaryDTO;
import com.cts.identity_service.dto.ResponseDTO.AdminUserResponseDTO;
import com.cts.identity_service.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryDTO toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public static AdminUserResponseDTO toDTO(User user){
        AdminUserResponseDTO dto = new AdminUserResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setDeleted(user.isDeleted());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
