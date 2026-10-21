package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Overall dashboard summary metrics and statistics")
public class DashboardStatsDTO {

    @Schema(description = "Total number of candidates", example = "150")
    private long totalCandidates;

    @Schema(description = "New candidates added this month", example = "24")
    private long newCandidatesThisMonth;

    @Schema(description = "Total assessments created", example = "80")
    private long totalAssessments;

    @Schema(description = "Assessments currently in progress", example = "12")
    private long inProgressAssessments;

    @Schema(description = "Assessments pending start", example = "18")
    private long pendingAssessments;

    @Schema(description = "Assessments completed", example = "50")
    private long completedAssessments;

    @Schema(description = "Total questions in bank", example = "300")
    private long totalQuestions;

    @Schema(description = "Total question categories", example = "15")
    private long totalCategories;

    @Schema(description = "Average AI evaluation score", example = "78.5")
    private Double avgAiScore;

    @Schema(description = "Average AI evaluation score from previous month", example = "72.0")
    private Double avgAiScoreLastMonth;
}