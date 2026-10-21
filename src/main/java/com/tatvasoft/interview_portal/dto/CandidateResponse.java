package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload representing candidate details")
public class CandidateResponse {

    @Schema(description = "Candidate unique ID", example = "1")
    private Long id;

    @Schema(description = "Candidate's first name", example = "Alice")
    private String firstName;

    @Schema(description = "Candidate's last name", example = "Smith")
    private String lastName;

    @Schema(description = "Candidate's email address", example = "alice.smith@example.com")
    private String email;

    @Schema(description = "Years of experience", example = "3")
    private Integer experience;

    @Schema(description = "Target designation", example = "Java Developer")
    private String designation;

    @Schema(description = "Candidate active status", example = "true")
    private Boolean isActive;
}