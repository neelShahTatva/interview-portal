package com.tatvasoft.interview_portal.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Long roleId;
    private Boolean isActive;
}