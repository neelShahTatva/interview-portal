package com.tatvasoft.interview_portal.controller;

import com.tatvasoft.interview_portal.ai.service.AiSolutionGenerationService;
import com.tatvasoft.interview_portal.entity.QuestionSolution;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final AiSolutionGenerationService aiService;

    public AiTestController(
            AiSolutionGenerationService aiService
    ) {
        this.aiService = aiService;
    }

    @GetMapping("/test-ai")
    public QuestionSolution test() {

        return aiService.generateSolution(
                "Binary Search",
                "Implement binary search in Java"
        );
    }
}