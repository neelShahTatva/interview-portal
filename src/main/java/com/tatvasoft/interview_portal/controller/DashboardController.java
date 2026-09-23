package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboard", description = "Endpoints for dashboard analytics, statistics, status breakdowns, and pipeline metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Dashboard Summary Stats", description = "Retrieves high-level dashboard metrics (counts of candidates, assessments, questions, categories, and AI score averages).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard stats retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get Assessment Status Breakdown", description = "Retrieves count of assessments grouped by status within the specified number of days.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment status breakdown retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid days parameter supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/assessment-status")
    public ResponseEntity<Map<String, Object>> getAssessmentStatus(
            @Parameter(description = "Number of trailing days to include in the breakdown", example = "30")
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<AssessmentStatusDTO> result = dashboardService.getAssessmentStatusBreakdown(days);
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching assessment status breakdown", e);
            return error("Failed to fetch assessment status breakdown");
        }
    }

    @Operation(summary = "Get Candidate Pipeline Metrics", description = "Retrieves recruitment pipeline metrics including applied, assessed, evaluated, shortlisted counts and designation breakdowns.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidate pipeline metrics retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get Recent Submissions", description = "Retrieves the most recent candidate assessment submissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recent submissions retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid limit parameter supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recent-submissions")
    public ResponseEntity<Map<String, Object>> getRecentSubmissions(
            @Parameter(description = "Maximum number of recent submissions to return", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<RecentSubmissionDTO> result = dashboardService.getRecentSubmissions(limit);
            return ok("Fetched successfully", result);
        } catch (Exception e) {
            log.error("Error fetching recent submissions", e);
            return error("Failed to fetch recent submissions");
        }
    }

    @Operation(summary = "Get Questions by Difficulty", description = "Retrieves question distribution across difficulty levels (EASY, MEDIUM, HARD).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions by difficulty retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get AI Score Distribution", description = "Retrieves the score distribution frequency across all evaluated candidates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI score distribution retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get Recent Activity Log", description = "Retrieves recent system events and actions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recent activity retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid limit parameter supplied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity(
            @Parameter(description = "Maximum number of activity records to return", example = "5")
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
