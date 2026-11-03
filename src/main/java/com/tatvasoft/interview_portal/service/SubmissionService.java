package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.submission.SubmissionRequest;
import com.tatvasoft.interview_portal.dto.submission.SubmissionResponse;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse create(SubmissionRequest request);

    List<SubmissionResponse> getAll();

    SubmissionResponse getById(Long id);

    SubmissionResponse update(Long id, SubmissionRequest request);

    void delete(Long id);
}
