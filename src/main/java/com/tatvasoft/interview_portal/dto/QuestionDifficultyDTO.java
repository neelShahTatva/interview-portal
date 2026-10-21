package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Question count breakdown by difficulty level")
public class QuestionDifficultyDTO {

    @Schema(description = "Difficulty level (EASY, MEDIUM, HARD)", example = "MEDIUM")
    private String difficulty;

    @Schema(description = "Count of questions", example = "25")
    private long count;
}