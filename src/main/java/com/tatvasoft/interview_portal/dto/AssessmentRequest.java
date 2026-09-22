package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload for creating a new assessment")
public class AssessmentRequest {

    @Schema(description = "Candidate unique ID", example = "1")
    private Long candidateId;

    @Schema(description = "Title of the assessment", example = "Java Developer Assessment")
    @NotBlank(message = "Assessment title is required")
    @Size(
            min = 3,
            max = 100,
            message = "Assessment title must be between 3 and 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9 ._&()'\\-,:]*$",
            message = "Assessment title must start with a letter or number and may contain only letters, numbers, spaces, and common punctuation"
    )
    private String title;

    @Schema(description = "Assessment time limit in minutes", example = "60")
    @NotNull(message = "Assessment time limit is required")
    @Min(
            value = 30,
            message = "Assessment time limit must be at least 30 minutes"
    )
    @Max(
            value = 180,
            message = "Assessment time limit must not exceed 180 minutes"
    )
    private Integer timeLimitMinutes;

    @Schema(description = "List of question IDs to include in the assessment", example = "[1, 2, 3]")
    private List<Long> questionIds;
}