package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Assessment count aggregated by status")
public class AssessmentStatusDTO {

    @Schema(description = "Assessment status name", example = "COMPLETED")
    private String status;

    @Schema(description = "Total count of assessments in this status", example = "42")
    private long count;
}