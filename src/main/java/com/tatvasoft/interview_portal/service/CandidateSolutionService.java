package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.candidate.CandidateSolutionResponse;

import java.util.List;

public interface CandidateSolutionService {

    List<CandidateSolutionResponse>
    getBySubmissionId(Long submissionId);
}
