package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.service.AuthService;
import com.tatvasoft.interview_portal.service.EmailService;
import com.tatvasoft.interview_portal.util.JwtUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Authentication", description = "Endpoints for user authentication, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    public AuthController(JwtUtil jwtUtil, AuthService authService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
    }

    @Operation(summary = "User Login", description = "Authenticates user credentials and returns access and refresh JWT tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, loginResponse));
    }

    @Operation(summary = "Refresh Access Token", description = "Generates new access and refresh tokens using a valid refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<Map<String, String>>> refresh(
            @Parameter(description = "Valid JWT refresh token", required = true) @RequestParam String refreshToken) {
        Map<String, String> tokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, tokens));
    }

    @Operation(summary = "Forgot Password", description = "Initiates password reset flow by sending a reset link to the user's email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset password link sent successfully"),
            @ApiResponse(responseCode = "404", description = "User email not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.processForgotPassword(request);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, "Reset password link sent on email"));
    }

    @Operation(summary = "Reset Password", description = "Resets user password using the token received in email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token, or invalid password format")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.processResetPassword(request);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, "Password reset successfully"));
    }

    @Operation(summary = "Validate Reset Token", description = "Validates whether the provided password reset token is active and valid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Token is invalid or has expired")
    })
    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> validateResetToken(
            @Parameter(description = "Password reset token", required = true) @PathVariable String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, "Token is valid"));
    }
}
