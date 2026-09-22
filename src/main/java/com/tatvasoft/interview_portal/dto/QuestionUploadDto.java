package com.tatvasoft.interview_portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Uploaded question metadata with solution file information")
public class QuestionUploadDto {

    @Schema(description = "Question unique ID", example = "1")
    private Long questionId;

    @Schema(description = "Question title", example = "Reverse Linked List")
    private String questionTitle;

    @Schema(description = "Question problem description")
    private String questionDescription;

    @Schema(description = "Solution reference file ID", example = "10")
    private Long solutionFileId;

    @Schema(description = "Solution file name", example = "solution.java")
    private String solutionFileName;
}