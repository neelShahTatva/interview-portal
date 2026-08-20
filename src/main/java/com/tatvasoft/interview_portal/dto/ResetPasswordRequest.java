package com.tatvasoft.interview_portal.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
}