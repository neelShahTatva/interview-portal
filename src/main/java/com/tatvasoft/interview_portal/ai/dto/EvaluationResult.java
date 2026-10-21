package com.tatvasoft.interview_portal.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI code evaluation result details")
public class EvaluationResult {

    @Schema(description = "Overall AI score (0-100)", example = "85")
    private int score;

    @Schema(description = "Detailed AI evaluation feedback", example = "The solution is correct and handles edge cases well.")
    private String feedback;

    @Schema(description = "Estimated time complexity", example = "O(n)")
    private String timeComplexity;

    @Schema(description = "Estimated space complexity", example = "O(1)")
    private String spaceComplexity;

    @Schema(description = "List of unhandled edge cases identified by AI", example = "[\"Null array input\", \"Empty string\"]")
    private List<String> missedEdgeCases;

    @Schema(description = "List of potential security vulnerabilities identified by AI", example = "[\"Potential NullPointerException\"]")
    private List<String> securityIssues;

    @Schema(description = "AI optimized reference code suggestion")
    private String optimizedCode;
}
