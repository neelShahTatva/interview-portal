package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recent submission summary")
public class RecentSubmissionDTO {

    @Schema(description = "Submission unique ID", example = "1")
    private Long submissionId;

    @Schema(description = "Candidate unique ID", example = "5")
    private Long candidateId;

    @Schema(description = "Candidate full name", example = "Alice Smith")
    private String candidateName;

    @Schema(description = "Target designation", example = "Java Developer")
    private String designation;

    @Schema(description = "Programming language used", example = "Java")
    private String language;

    @Schema(description = "AI awarded score (0-100)", example = "85")
    private Integer aiScore;

    @Schema(description = "Evaluation timestamp")
    private LocalDateTime evaluatedAt;
}