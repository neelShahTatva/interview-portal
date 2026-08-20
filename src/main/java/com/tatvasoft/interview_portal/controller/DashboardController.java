package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INTERVIEWER')")
public class DashboardController {

    private final DashboardService dashboardService;

    // 1. Stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            DashboardStatsDTO stats = dashboardService.getStats();
            return ok("Fetched successfully", stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats", e);
            return error("Failed to fetch dashboard stats");
        }
    }

    // 2. Assessment Status Breakdown
    @GetMapping("/assessment-status")
    public ResponseEntity<Map<String, Object>> getAssessmentStatus(
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<AssessmentStatusDTO> result = dashboardService.getAssessmentStatusBreakdown(days);
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching assessment status breakdown", e);
            return error("Failed to fetch assessment status breakdown");
        }
    }

    // 3. Candidate Pipeline
    @GetMapping("/candidate-pipeline")
    public ResponseEntity<Map<String, Object>> getCandidatePipeline() {
        try {
            CandidatePipelineDTO result = dashboardService.getCandidatePipeline();
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching candidate pipeline", e);
            return error("Failed to fetch candidate pipeline");
        }
    }

    // 4. Recent Submissions
    @GetMapping("/recent-submissions")
    public ResponseEntity<Map<String, Object>> getRecentSubmissions(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<RecentSubmissionDTO> result = dashboardService.getRecentSubmissions(limit);
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching recent submissions", e);
            return error("Failed to fetch recent submissions");
        }
    }

    // 5. Questions by Difficulty
    @GetMapping("/questions-by-difficulty")
    public ResponseEntity<Map<String, Object>> getQuestionsByDifficulty() {
        try {
            List<QuestionDifficultyDTO> result = dashboardService.getQuestionsByDifficulty();
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching questions by difficulty", e);
            return error("Failed to fetch questions by difficulty");
        }
    }

    // 6. AI Score Distribution
    @GetMapping("/ai-score-distribution")
    public ResponseEntity<Map<String, Object>> getAiScoreDistribution() {
        try {
            List<AiScoreDistributionDTO> result = dashboardService.getAiScoreDistribution();
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching AI score distribution", e);
            return error("Failed to fetch AI score distribution");
        }
    }

    // 7. Recent Activity
    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<RecentActivityDTO> result = dashboardService.getRecentActivity(limit);
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching recent activity", e);
            return error("Failed to fetch recent activity");
        }
    }

    // Response Helpers
    private ResponseEntity<Map<String, Object>> ok(String message, Object result) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("result", result);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("result", null);
        return ResponseEntity.internalServerError().body(body);
    }
}
