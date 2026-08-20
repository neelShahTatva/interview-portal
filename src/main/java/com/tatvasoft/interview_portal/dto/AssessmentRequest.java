package com.tatvasoft.interview_portal.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentRequest {

    private Long candidateId;

    private String title;

    private Integer timeLimitMinutes;

    private List<Long> questionIds;
}