package com.tatvasoft.interview_portal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendedRequestDto {

    private Long questionId;

    private String title;

    private String description;

    private Integer estimatedMinutes;

}
