package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request payload for creating or updating a question")
public class QuestionRequest {

    @Schema(description = "Question title / problem statement", example = "Two Sum", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Detailed problem description and constraints", example = "Given an array of integers nums and an integer target...")
    private String description;

    @Schema(description = "Difficulty level (EASY, MEDIUM, HARD)", example = "MEDIUM")
    private String difficulty;

    @Schema(description = "Estimated time in minutes to solve", example = "30")
    private Integer estimatedTime;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether to automatically generate AI solution", example = "true")
    private Boolean generateAiSolution;

    @Schema(description = "List of applicable job designations", example = "[\"Java Developer\", \"Full Stack Developer\"]")
    private List<String> designations;

    @Schema(description = "List of category IDs associated with this question", example = "[1, 2]")
    private List<Long> categoryIds;

    @Schema(description = "List of reference solutions in different languages")
    private List<SolutionDto> solutions;
}
