package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentSubmissionDTO {

    private Long submissionId;
    private Long candidateId;
    private String candidateName;
    private String designation;
    private String language;
    private Integer aiScore;
    private LocalDateTime evaluatedAt;
}