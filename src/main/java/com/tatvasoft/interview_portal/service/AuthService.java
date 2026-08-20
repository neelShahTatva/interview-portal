package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.ForgotPasswordRequest;
import com.tatvasoft.interview_portal.dto.LoginRequest;
import com.tatvasoft.interview_portal.dto.LoginResponse;
import com.tatvasoft.interview_portal.dto.ResetPasswordRequest;

import java.util.Map;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Map<String, String> refreshAccessToken(String refreshToken);
    void processForgotPassword(ForgotPasswordRequest request);
    void processResetPassword(ResetPasswordRequest request);
    void validateResetToken(String token);
}
