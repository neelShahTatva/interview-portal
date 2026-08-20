package com.tatvasoft.interview_portal.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MultiQuestionEvaluationResult {

    private Boolean isSuccess;

    private Double overallScore;

    private Integer totalQuestions;

    private List<QuestionEvaluationResult> evaluations;

}