package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Category DTO")
public class CategoryDto {

    @Schema(description = "Category unique ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Data Structures")
    private String name;
}
