package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for checking question similarity")
public class QuestionSimilarityCheckRequest {

    @Schema(description = "Optional ID of existing question to exclude (used in edit mode)", example = "5")
    private Long id;

    @Schema(description = "Title of the question to check", example = "Two Sum", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Description of the question", example = "Find two numbers in array that add up to target")
    private String description;
}
