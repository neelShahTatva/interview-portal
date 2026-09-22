package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Category response details")
public class CategoryResponse {

    @Schema(description = "Category unique ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Algorithms")
    private String name;
}
