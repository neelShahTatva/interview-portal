package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.service.AuthService;
import com.tatvasoft.interview_portal.service.EmailService;
import com.tatvasoft.interview_portal.service.UserService;
import com.tatvasoft.interview_portal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, AuthService authService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, true, null, loginResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestParam String refreshToken) {
        Map<String, String> tokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>(200, true, null, tokens));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.processForgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, true, null, "Reset password link sent on email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.processResetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, true, null, "Password reset successfully"));
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<ApiResponse<String>> validateResetToken(@PathVariable String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(new ApiResponse<>(200, true, null, "Token is valid"));
    }
}
