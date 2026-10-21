package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Candidate hiring pipeline metrics and breakdown")
public class CandidatePipelineDTO {

    @Schema(description = "Total number of candidates applied", example = "120")
    private long totalApplied;

    @Schema(description = "Total number of candidates who completed assessments", example = "85")
    private long totalAssessed;

    @Schema(description = "Total number of candidates evaluated by AI", example = "60")
    private long totalEvaluated;

    @Schema(description = "Total number of candidates shortlisted", example = "25")
    private long totalShortlisted;

    @Schema(description = "Candidate count grouped by designation")
    private List<DesignationCountDTO> byDesignation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Candidate count per designation")
    public static class DesignationCountDTO {
        @Schema(description = "Designation name", example = "Java Developer")
        private String designation;

        @Schema(description = "Count of candidates", example = "15")
        private long count;
    }
}