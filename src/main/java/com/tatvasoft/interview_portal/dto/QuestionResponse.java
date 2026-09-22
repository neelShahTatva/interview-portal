package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response payload representing a question")
public class QuestionResponse {

    @Schema(description = "Question unique ID", example = "1")
    private Long id;

    @Schema(description = "Question title", example = "Two Sum")
    private String title;

    @Schema(description = "Detailed problem description", example = "Given an array of integers nums and an integer target...")
    private String description;

    @Schema(description = "List of applicable job designations", example = "[\"Java Developer\", \"Full Stack Developer\"]")
    private List<String> designations;

    @Schema(description = "Difficulty level (EASY, MEDIUM, HARD)", example = "MEDIUM")
    private String difficulty;

    @Schema(description = "Estimated time in minutes to solve", example = "30")
    private Integer estimatedTime;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "List of associated categories")
    private List<CategoryDto> categories;

    @Schema(description = "List of reference solutions")
    private List<SolutionDto> solutions;
}
