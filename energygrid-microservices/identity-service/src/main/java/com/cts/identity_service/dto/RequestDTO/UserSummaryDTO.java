package com.cts.identity_service.dto.RequestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryDTO {
    private Long userId;
    private String username;
    private String role;
}