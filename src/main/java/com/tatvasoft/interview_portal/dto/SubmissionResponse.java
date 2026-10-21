package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload representing candidate submission details")
public class SubmissionResponse {

    @Schema(description = "Submission unique ID", example = "1")
    private Long id;

    @Schema(description = "Assessment unique ID", example = "1")
    private Long assessmentId;

    @Schema(description = "Assessment title", example = "Java Developer Assessment")
    private String assessmentName;

    @Schema(description = "Reference file ID", example = "1")
    private Long referenceFileId;

    @Schema(description = "Candidate unique ID", example = "1")
    private Long candidateId;

    @Schema(description = "Candidate full name", example = "Alice Smith")
    private String candidateName;

    @Schema(description = "Candidate submitted code")
    private String code;

    @Schema(description = "Candidate output")
    private String output;

    @Schema(description = "AI awarded score (0-100)", example = "88")
    private Integer aiScore;

    @Schema(description = "AI generated feedback")
    private String aiFeedback;

    @Schema(description = "Evaluation timestamp")
    private LocalDateTime evaluatedAt;

    @Schema(description = "Submission creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "User ID who created the submission", example = "1")
    private Long createdBy;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "User ID who updated the submission", example = "1")
    private Long updatedBy;
}
