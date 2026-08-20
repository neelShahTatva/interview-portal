package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalCandidates;
    private long newCandidatesThisMonth;

    private long totalAssessments;
    private long inProgressAssessments;
    private long pendingAssessments;
    private long completedAssessments;

    private long totalQuestions;
    private long totalCategories;

    private Double avgAiScore;
    private Double avgAiScoreLastMonth;
}