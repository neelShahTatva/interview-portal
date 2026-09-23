package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.ai.service.AiSolutionGenerationService;
import com.tatvasoft.interview_portal.entity.QuestionSolution;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@Tag(name = "AI Testing", description = "Internal/testing endpoints for verifying AI solution generation")
public class AiTestController {

    private final AiSolutionGenerationService aiService;

    public AiTestController(
            AiSolutionGenerationService aiService
    ) {
        this.aiService = aiService;
    }

    @Operation(summary = "Test AI Solution Generation", description = "Generates a sample reference solution for Binary Search using the active AI provider.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI solution generated successfully"),
            @ApiResponse(responseCode = "500", description = "AI generation failure")
    })
    @GetMapping("/test-ai")
    public QuestionSolution test() {

        return aiService.generateSolution(
                "Binary Search",
                "Implement binary search in Java"
        );
    }
}