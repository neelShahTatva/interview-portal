package com.tatvasoft.interview_portal.dto;

import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Response payload containing candidate evaluation and question summary")
public class CandidateEvaluationResponse {

    @Schema(description = "Whether the candidate has been evaluated", example = "true")
    private Boolean isEvaluated;

    @Schema(description = "Assessment unique ID", example = "10")
    private Long assessmentId;

    @Schema(description = "Candidate unique ID", example = "5")
    private Long candidateId;

    @Schema(description = "Title of the assessment", example = "Java Backend Developer Assessment")
    private String assessmentTitle;

    @Schema(description = "AI evaluation result details")
    private MultiQuestionEvaluationResult evaluation;

    @Schema(description = "List of questions included in the assessment")
    private List<QuestionUploadDto> questions;
}