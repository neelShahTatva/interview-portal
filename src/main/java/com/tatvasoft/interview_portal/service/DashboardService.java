package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.*;

import java.util.List;

public interface DashboardService {

    DashboardStatsDTO getStats();

    List<AssessmentStatusDTO> getAssessmentStatusBreakdown(int days);

    CandidatePipelineDTO getCandidatePipeline();

    List<RecentSubmissionDTO> getRecentSubmissions(int limit);

    List<QuestionDifficultyDTO> getQuestionsByDifficulty();

    List<AiScoreDistributionDTO> getAiScoreDistribution();

    List<RecentActivityDTO> getRecentActivity(int limit);
}