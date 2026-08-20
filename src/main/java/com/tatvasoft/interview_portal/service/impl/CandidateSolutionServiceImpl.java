package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.CandidateSolutionResponse;
import com.tatvasoft.interview_portal.entity.CandidateSolution;
import com.tatvasoft.interview_portal.repository.CandidateSolutionRepository;
import com.tatvasoft.interview_portal.repository.QuestionsRepository;
import com.tatvasoft.interview_portal.service.CandidateSolutionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateSolutionServiceImpl
        implements CandidateSolutionService {

    private final CandidateSolutionRepository repository;
    private final QuestionsRepository questionRepository;

    public CandidateSolutionServiceImpl(
            CandidateSolutionRepository repository,
            QuestionsRepository questionRepository) {

        this.repository = repository;
        this.questionRepository = questionRepository;
    }

    @Override
    public List<CandidateSolutionResponse>
    getBySubmissionId(Long submissionId) {

        return repository
                .findBySubmissionId(submissionId)
                .stream()
                .map(this::map)
                .toList();
    }

    private CandidateSolutionResponse map(
            CandidateSolution solution) {

        String questionTitle =
                questionRepository
                        .findById(solution.getQuestionId())
                        .map(q -> q.getTitle())
                        .orElse("");

        return new CandidateSolutionResponse(
                solution.getId(),
                solution.getSubmissionId(),
                solution.getQuestionId(),
                questionTitle,
                solution.getSolution(),
                solution.getAiScore(),
                solution.getAiFeedback()
        );
    }
}
