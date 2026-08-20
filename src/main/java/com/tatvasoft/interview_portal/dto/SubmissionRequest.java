package com.tatvasoft.interview_portal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionRequest {

    private Long assessmentId;
    private Long referenceFileId;
    private Long candidateId;

    private String code;
    private String output;

    private Integer aiScore;
    private String aiFeedback;

    private LocalDateTime evaluatedAt;

    private Long createdBy;
    private Long updatedBy;
}
