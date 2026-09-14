package com.tatvasoft.interview_portal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentRequest {

    private Long candidateId;

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

    private List<Long> questionIds;
}