package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "AI recommended questions response payload")
public class QuestionRecommendedResponse {

    @Schema(description = "Total estimated minutes for all recommended questions", example = "90")
    private Integer totalEstimatedMinutes;

    @Schema(description = "List of recommended questions")
    private List<RecommendedRequestDto> questions;
}
