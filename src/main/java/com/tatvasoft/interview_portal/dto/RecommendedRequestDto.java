package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Recommended question details")
public class RecommendedRequestDto {

    @Schema(description = "Question unique ID", example = "1")
    private Long questionId;

    @Schema(description = "Question title", example = "Two Sum")
    private String title;

    @Schema(description = "Question description", example = "Given an array of integers...")
    private String description;

    @Schema(description = "Estimated time in minutes to solve", example = "30")
    private Integer estimatedMinutes;

}
