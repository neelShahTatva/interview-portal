package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.ForgotPasswordRequest;
import com.tatvasoft.interview_portal.dto.LoginRequest;
import com.tatvasoft.interview_portal.dto.LoginResponse;
import com.tatvasoft.interview_portal.dto.ResetPasswordRequest;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.exception.AccountInactiveException;
import com.tatvasoft.interview_portal.exception.InvalidCredentialsException;
import com.tatvasoft.interview_portal.exception.ResourceNotFoundException;
import com.tatvasoft.interview_portal.exception.TokenValidationException;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.service.AuthService;
import com.tatvasoft.interview_portal.service.EmailService;
import com.tatvasoft.interview_portal.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AccountInactiveException("User account is inactive");
        }

        return new LoginResponse(
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user),
                user
        );
    }

    @Override
    public Map<String, String> refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !"refresh".equals(jwtUtil.extractType(refreshToken))) {
            throw new TokenValidationException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(jwtUtil.extractUsername(refreshToken))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return Map.of("accessToken", jwtUtil.generateAccessToken(user));
    }

    @Override
    public void processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendResetPasswordEmail(user.getEmail(), "http://localhost:4200/auth/reset-password/" + token);
    }

    @Override
    public void processResetPassword(ResetPasswordRequest request) {
        User user = getValidTokenUser(request.getToken());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void validateResetToken(String token) {
        getValidTokenUser(token); // Throws exception if invalid
    }

    // Helper method to DRY up token validation logic
    private User getValidTokenUser(String token) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new TokenValidationException("Invalid reset token"));
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenValidationException("Reset token expired");
        }
        return user;
    }
}
