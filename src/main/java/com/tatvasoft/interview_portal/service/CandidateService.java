package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.candidate.CandidateEvaluationResponse;
import com.tatvasoft.interview_portal.dto.candidate.CandidateRequest;
import com.tatvasoft.interview_portal.dto.candidate.CandidateResponse;

import java.util.List;

public interface CandidateService {

    CandidateResponse create(CandidateRequest request);

    List<CandidateResponse> getAll();

    CandidateResponse getById(Long id);

    CandidateResponse update(Long id, CandidateRequest request);
    CandidateEvaluationResponse getCandidateEvaluation(Long candidateId);
    void delete(Long id, String comment);
}