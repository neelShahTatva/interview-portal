package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request payload for creating or updating a candidate submission")
public class SubmissionRequest {

    @Schema(description = "Assessment unique ID", example = "1")
    private Long assessmentId;

    @Schema(description = "Reference file ID", example = "1")
    private Long referenceFileId;

    @Schema(description = "Candidate unique ID", example = "1")
    private Long candidateId;

    @Schema(description = "Candidate's submitted code or file content")
    private String code;

    @Schema(description = "Candidate code execution output")
    private String output;

    @Schema(description = "AI awarded score (0-100)", example = "88")
    private Integer aiScore;

    @Schema(description = "AI generated feedback text")
    private String aiFeedback;

    @Schema(description = "Timestamp when AI evaluated the submission")
    private LocalDateTime evaluatedAt;

    @Schema(description = "User ID who created the record", example = "1")
    private Long createdBy;

    @Schema(description = "User ID who updated the record", example = "1")
    private Long updatedBy;
}
