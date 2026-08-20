package com.tatvasoft.interview_portal.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuestionEvaluationResult {
    private Long questionId;

    private Integer questionNumber;

    private String questionTopic;

    private Integer score;

    private String feedback;

    private String timeComplexity;

    private String spaceComplexity;

    private List<String> missedEdgeCases;

    private List<String> securityIssues;

    private String optimizedCode;

    private String candidateCode;
}