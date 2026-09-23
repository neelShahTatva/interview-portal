package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload containing candidate solution and AI evaluation for a question")
public class CandidateSolutionResponse {

    @Schema(description = "Solution unique ID", example = "1")
    private Long id;

    @Schema(description = "Submission unique ID", example = "10")
    private Long submissionId;

    @Schema(description = "Question unique ID", example = "4")
    private Long questionId;

    @Schema(description = "Question title", example = "Two Sum")
    private String questionTitle;

    @Schema(description = "Candidate's submitted solution code", example = "class Solution { ... }")
    private String solution;

    @Schema(description = "AI awarded score (0-100)", example = "85")
    private Integer aiScore;

    @Schema(description = "AI feedback and suggestions", example = "Optimal time complexity achieved.")
    private String aiFeedback;
}
