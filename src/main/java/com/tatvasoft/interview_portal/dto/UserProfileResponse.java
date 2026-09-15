package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private Long roleId;
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String profilePictureUrl;
    private String token;
}
