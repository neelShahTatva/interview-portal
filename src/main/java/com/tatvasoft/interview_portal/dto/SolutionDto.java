package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Solution reference code details")
public class SolutionDto {

    @Schema(description = "Programming language (e.g. Java, Python, C++)", example = "Java")
    private String language;

    @Schema(description = "Reference solution code", example = "public class Solution { ... }")
    private String solutionCode;
}
