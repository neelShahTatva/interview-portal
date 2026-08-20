package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.repository.DashboardRepository;
import com.tatvasoft.interview_portal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    // 1. Stats

    @Override
    public DashboardStatsDTO getStats() {
        return DashboardStatsDTO.builder()
                .totalCandidates(dashboardRepository.countTotalCandidates())
                .newCandidatesThisMonth(dashboardRepository.countNewCandidatesThisMonth())
                .totalAssessments(dashboardRepository.countTotalAssessments())
                .inProgressAssessments(dashboardRepository.countInProgressAssessments())
                .pendingAssessments(dashboardRepository.countPendingAssessments())
                .completedAssessments(dashboardRepository.countCompletedAssessments())
                .totalQuestions(dashboardRepository.countTotalQuestions())
                .totalCategories(dashboardRepository.countTotalCategories())
                .avgAiScore(roundToOneDecimal(dashboardRepository.avgAiScore()))
                .avgAiScoreLastMonth(roundToOneDecimal(dashboardRepository.avgAiScoreLastMonth()))
                .build();
    }

    // 2. Assessment Status Breakdown

    @Override
    public List<AssessmentStatusDTO> getAssessmentStatusBreakdown(int days) {
        List<Object[]> rows = dashboardRepository.findAssessmentStatusBreakdown(days);
        return rows.stream()
                .map(row -> new AssessmentStatusDTO(
                        (String) row[0],
                        toLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    // 3. Candidate Pipeline

    @Override
    public CandidatePipelineDTO getCandidatePipeline() {
        List<Object[]> designationRows = dashboardRepository.findCandidatesByDesignation();

        List<CandidatePipelineDTO.DesignationCountDTO> byDesignation = designationRows.stream()
                .map(row -> new CandidatePipelineDTO.DesignationCountDTO(
                        (String) row[0],
                        toLong(row[1])
                ))
                .collect(Collectors.toList());

        return CandidatePipelineDTO.builder()
                .totalApplied(dashboardRepository.countTotalApplied())
                .totalAssessed(dashboardRepository.countTotalAssessed())
                .totalEvaluated(dashboardRepository.countTotalEvaluated())
                .totalShortlisted(0L) // no shortlisting column in current schema; extend when available
                .byDesignation(byDesignation)
                .build();
    }

    // 4. Recent Submissions

    @Override
    public List<RecentSubmissionDTO> getRecentSubmissions(int limit) {
        List<Object[]> rows = dashboardRepository.findRecentSubmissions(limit);
        return rows.stream()
                .map(row -> new RecentSubmissionDTO(
                        toLong(row[0]),
                        toLong(row[1]),
                        (String) row[2],
                        (String) row[3],
                        (String) row[4],
                        toInt(row[5]),
                        toLocalDateTime(row[6])
                ))
                .collect(Collectors.toList());
    }

    // 5. Questions by Difficulty

    @Override
    public List<QuestionDifficultyDTO> getQuestionsByDifficulty() {
        List<Object[]> rows = dashboardRepository.findQuestionsByDifficulty();

        // Build a map from DB results
        Map<String, Long> dbMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> toLong(row[1])
                ));

        // Always return all 3 difficulty levels, even if count is 0
        List<String> allDifficulties = List.of("EASY", "MEDIUM", "HARD");
        return allDifficulties.stream()
                .map(d -> new QuestionDifficultyDTO(d, dbMap.getOrDefault(d, 0L)))
                .collect(Collectors.toList());
    }

    // 6. AI Score Distribution

    @Override
    public List<AiScoreDistributionDTO> getAiScoreDistribution() {
        List<Object[]> rows = dashboardRepository.findAiScoreDistribution();

        // Build a map from DB results (score → count)
        Map<Integer, Long> dbMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> toInt(row[0]),
                        row -> toLong(row[1])
                ));

        // Fill all scores 1–10; missing scores get count 0
        List<AiScoreDistributionDTO> result = new ArrayList<>();
        for (int score = 1; score <= 10; score++) {
            result.add(new AiScoreDistributionDTO(score, dbMap.getOrDefault(score, 0L)));
        }
        return result;
    }

    // 7. Recent Activity

    @Override
    public List<RecentActivityDTO> getRecentActivity(int limit) {
        List<Object[]> rows = dashboardRepository.findRecentActivity(limit);
        return rows.stream()
                .map(row -> new RecentActivityDTO(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        toLocalDateTime(row[3])
                ))
                .collect(Collectors.toList());
    }

    // Helpers

    private Double roundToOneDecimal(Double value) {
        if (value == null) return null;
        return Math.round(value * 10.0) / 10.0;
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long l) return l;
        if (obj instanceof Number n) return n.longValue();
        return Long.parseLong(obj.toString());
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Number n) return n.intValue();
        return Integer.parseInt(obj.toString());
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime ldt) return ldt;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}