package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;

    private Long candidateId;

    private String status;

    private Integer timeLimitMinutes;

    private Boolean isActive;

    private String title;

    private List<QuestionResponse> questions;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime updatedAt;

    private Long updatedBy;
}