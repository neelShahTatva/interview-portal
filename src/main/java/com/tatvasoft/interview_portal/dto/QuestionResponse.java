package com.tatvasoft.interview_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> designations;
    private String difficulty;
    private Integer estimatedTime;
    private Boolean isActive;
    private List<CategoryDto> categories;
    private List<SolutionDto> solutions;
}
