package com.tatvasoft.interview_portal.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "Overall multi-question evaluation results")
public class MultiQuestionEvaluationResult {

    @Schema(description = "Whether the multi-question evaluation succeeded", example = "true")
    private Boolean isSuccess;

    @Schema(description = "Overall average evaluation score (0-100)", example = "82")
    private Integer overallScore;

    @Schema(description = "Total questions evaluated", example = "3")
    private Integer totalQuestions;

    @Schema(description = "Individual question evaluation breakdowns")
    private List<QuestionEvaluationResult> evaluations;

}