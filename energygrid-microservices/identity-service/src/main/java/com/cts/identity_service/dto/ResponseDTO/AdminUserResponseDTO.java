package com.cts.identity_service.dto.ResponseDTO;
import com.cts.identity_service.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminUserResponseDTO {

    private Long userId;
    private String name;
    private String username;
    private String email;
    private String phone;
    private User.Role role;
    private User.Status status;
    private boolean deleted;
    private LocalDateTime createdAt;

}
