package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CandidateSolutionResponse {

    private Long id;

    private Long submissionId;

    private Long questionId;

    private String questionTitle;

    private String solution;

    private Integer aiScore;

    private String aiFeedback;
}
