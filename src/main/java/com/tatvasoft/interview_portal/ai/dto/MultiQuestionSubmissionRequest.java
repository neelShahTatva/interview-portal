package com.tatvasoft.interview_portal.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MultiQuestionSubmissionRequest {
    private List<FileSubmissionRequest> questions;

}