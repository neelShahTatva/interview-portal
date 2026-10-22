package com.tatvasoft.interview_portal.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "Evaluation result for a specific question")
public class QuestionEvaluationResult {

    @Schema(description = "Question unique ID", example = "1")
    private Long questionId;

    @Schema(description = "Question sequential index", example = "1")
    private Integer questionNumber;

    @Schema(description = "Question topic or title", example = "Arrays & Hashing")
    private String questionTopic;

    @Schema(description = "Question evaluation score (0-100)", example = "85")
    private Integer score;

    @Schema(description = "Detailed AI feedback for this question")
    private String feedback;

    @Schema(description = "Estimated time complexity", example = "O(n)")
    private String timeComplexity;

    @Schema(description = "Estimated space complexity", example = "O(1)")
    private String spaceComplexity;

    @Schema(description = "Missed edge cases identified by AI")
    private List<String> missedEdgeCases;

    @Schema(description = "Identified security or performance issues")
    private List<String> securityIssues;

    @Schema(description = "AI optimized reference implementation")
    private String optimizedCode;

    @Schema(description = "Candidate submitted code")
    private String candidateCode;
}