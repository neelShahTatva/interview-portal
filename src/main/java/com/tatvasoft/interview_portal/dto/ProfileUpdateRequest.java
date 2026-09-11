package com.tatvasoft.interview_portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]+(?: [a-zA-Z0-9]+)*$",
        message = "Enter a valid username using letters, numbers and spaces only"
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Please provide a valid email address"
    )
    @Size(max = 255, message = "Email should not exceed 255 characters")
    private String email;

    private String password;

    private Long roleId;

    private Boolean isActive;
}
