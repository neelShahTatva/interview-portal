package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.dto.ai.AiScoreDistributionDTO;
import com.tatvasoft.interview_portal.dto.assessment.AssessmentStatusDTO;
import com.tatvasoft.interview_portal.dto.candidate.CandidatePipelineDTO;
import com.tatvasoft.interview_portal.dto.question.QuestionDifficultyDTO;

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