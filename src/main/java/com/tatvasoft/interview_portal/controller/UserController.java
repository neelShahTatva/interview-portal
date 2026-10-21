package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "Endpoints for managing user accounts, profiles, and avatar uploads")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create User", description = "Creates a new user in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse user = userService.createUser(request);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, user)
        );
    }

    @Operation(summary = "Get All Users", description = "Retrieves a list of all registered users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user list"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<List<UserResponse>>> getAllUsers() {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        userService.getAllUsers()
                )
        );
    }

    @Operation(summary = "Get User by ID", description = "Fetches a specific user's details by their user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details found"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID supplied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<UserResponse>> getUser(
            @Parameter(description = "Unique ID of the user", required = true) @PathVariable Long id) {

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        userService.getUserById(id)
                )
        );
    }

    @Operation(summary = "Update User", description = "Updates details of an existing user by their user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update payload or validation failure"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "Unique ID of the user to update", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse user =
                userService.updateUser(id, request);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        user
                )
        );
    }

    @Operation(summary = "Delete User", description = "Deletes a user by their user ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID supplied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> deleteUser(
            @Parameter(description = "Unique ID of the user to delete", required = true) @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(
                        200,
                        true,
                        null,
                        "User deleted successfully"
                )
        );
    }

    @Operation(summary = "Get Current User Profile", description = "Retrieves profile information of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/profile")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<UserProfileResponse>> getProfile() {
        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, userService.getProfile())
        );
    }

    @Operation(summary = "Update Current User Profile", description = "Updates profile details for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/profile")
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(
                new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, userService.updateProfile(request))
        );
    }

    @Operation(summary = "Upload Profile Picture", description = "Uploads a new profile picture avatar for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.tatvasoft.interview_portal.dto.ApiResponse<String>> uploadProfilePicture(
            @Parameter(description = "Profile image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        String filename = userService.uploadProfilePicture(file);
        return ResponseEntity.ok(new com.tatvasoft.interview_portal.dto.ApiResponse<>(200, true, null, filename));
    }
}