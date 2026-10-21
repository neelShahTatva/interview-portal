package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload representing an assessment")
public class AssessmentResponse {

    @Schema(description = "Assessment unique ID", example = "1")
    private Long id;

    @Schema(description = "Candidate unique ID", example = "1")
    private Long candidateId;

    @Schema(description = "Status of the assessment (e.g. PENDING, IN_PROGRESS, COMPLETED, EXPIRED)", example = "PENDING")
    private String status;

    @Schema(description = "Time limit in minutes", example = "60")
    private Integer timeLimitMinutes;

    @Schema(description = "Whether the assessment is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Title of the assessment", example = "Java Developer Assessment")
    private String title;

    @Schema(description = "List of questions in the assessment")
    private List<QuestionResponse> questions;

    @Schema(description = "Timestamp when assessment started")
    private LocalDateTime startedAt;

    @Schema(description = "Timestamp when assessment completed")
    private LocalDateTime completedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "User ID who created the assessment", example = "1")
    private Long createdBy;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "User ID who last updated the assessment", example = "1")
    private Long updatedBy;
}