package com.tatvasoft.interview_portal.dto;

import com.tatvasoft.interview_portal.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private User user;
}
