package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubmissionResponse {

    private Long id;

    private Long assessmentId;
    private String assessmentName;

    private Long referenceFileId;

    private Long candidateId;
    private String candidateName;

    private String code;
    private String output;

    private Integer aiScore;
    private String aiFeedback;

    private LocalDateTime evaluatedAt;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}
