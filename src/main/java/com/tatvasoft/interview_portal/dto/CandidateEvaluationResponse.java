package com.tatvasoft.interview_portal.dto;

import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CandidateEvaluationResponse {

    private Boolean isEvaluated;

    private Long assessmentId;

    private Long candidateId;

    private String assessmentTitle;

    private MultiQuestionEvaluationResult evaluation;

    private List<QuestionUploadDto> questions;
}