package com.tatvasoft.interview_portal.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private Long roleId;
    private Boolean isActive;
}