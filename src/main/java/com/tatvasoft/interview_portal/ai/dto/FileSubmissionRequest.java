package com.tatvasoft.interview_portal.ai.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Setter
@Getter
public class FileSubmissionRequest {
    private Long questionId;

    private Long assessmentId;

    private Long candidateId;

    private MultipartFile submissionFile;
}