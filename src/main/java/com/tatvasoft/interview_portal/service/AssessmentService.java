package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.AssessmentRequest;
import com.tatvasoft.interview_portal.dto.AssessmentResponse;
import com.tatvasoft.interview_portal.dto.CandidateResponse;

import java.util.List;

public interface AssessmentService {

    AssessmentResponse create(AssessmentRequest request);

    List<AssessmentResponse> getAll();

    AssessmentResponse getById(Long id);

//    AssessmentResponse update(Long id, AssessmentRequest request);

    void delete(Long id);

    void changeStatus(Long id, String status);

    List<CandidateResponse> getAvailableCandidates();
}
