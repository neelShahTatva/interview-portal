package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI score distribution data point")
public class AiScoreDistributionDTO {

    @Schema(description = "Score bucket or score value", example = "80")
    private int score;

    @Schema(description = "Count of candidates or evaluations in this bucket", example = "12")
    private long count;
}