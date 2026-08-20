package com.tatvasoft.interview_portal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUploadDto {

    private Long questionId;

    private String questionTitle;

    private String questionDescription;

    private Long solutionFileId;

    private String solutionFileName;
}