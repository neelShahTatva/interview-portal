package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatePipelineDTO {

    private long totalApplied;
    private long totalAssessed;
    private long totalEvaluated;
    private long totalShortlisted;
    private List<DesignationCountDTO> byDesignation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignationCountDTO {
        private String designation;
        private long count;
    }
}