package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.SubmissionRequest;
import com.tatvasoft.interview_portal.dto.SubmissionResponse;
import com.tatvasoft.interview_portal.entity.Submission;
import com.tatvasoft.interview_portal.entity.User;
import com.tatvasoft.interview_portal.repository.AssessmentRepository;
import com.tatvasoft.interview_portal.repository.CandidateRepository;
import com.tatvasoft.interview_portal.repository.SubmissionRepository;
import com.tatvasoft.interview_portal.repository.UserRepository;
import com.tatvasoft.interview_portal.service.SubmissionService;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository repository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final CandidateRepository candidateRepository;

    public SubmissionServiceImpl(
            SubmissionRepository repository,
            UserRepository userRepository,
            AssessmentRepository assessmentRepository,
            CandidateRepository candidateRepository) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public SubmissionResponse create(SubmissionRequest request) {

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged in user not found"));

        Submission s = new Submission();

        s.setAssessmentId(request.getAssessmentId());
        s.setReferenceFileId(request.getReferenceFileId());
        s.setCandidateId(request.getCandidateId());

        s.setCode(request.getCode());
        s.setOutput(request.getOutput());

        s.setAiScore(request.getAiScore());
        s.setAiFeedback(request.getAiFeedback());

        s.setEvaluatedAt(request.getEvaluatedAt());

        s.setCreatedBy(currentUser.getId());
        s.setCreatedAt(LocalDateTime.now());

        return map(repository.save(s));
    }

    @Override
    public List<SubmissionResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public SubmissionResponse getById(Long id) {

        Submission s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        return map(s);
    }

    @Override
    public SubmissionResponse update(Long id, SubmissionRequest request) {

        Submission s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged in user not found"));

        s.setCode(request.getCode());
        s.setOutput(request.getOutput());

        s.setAiScore(request.getAiScore());
        s.setAiFeedback(request.getAiFeedback());

        s.setEvaluatedAt(request.getEvaluatedAt());

        s.setUpdatedBy(currentUser.getId());
        s.setUpdatedAt(LocalDateTime.now());

        return map(repository.save(s));
    }

    @Override
    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException("Submission not found");
        }

        repository.deleteById(id);
    }

    private SubmissionResponse map(Submission s) {

        String assessmentName =
                assessmentRepository
                        .findById(s.getAssessmentId())
                        .map(a -> a.getTitle())
                        .orElse("");

        String candidateName =
                candidateRepository
                        .findById(s.getCandidateId())
                        .map(c ->
                                c.getFirstName()
                                        + " "
                                        + c.getLastName())
                        .orElse("");

        return new SubmissionResponse(
                s.getId(),

                s.getAssessmentId(),
                assessmentName,

                s.getReferenceFileId(),

                s.getCandidateId(),
                candidateName,

                s.getCode(),
                s.getOutput(),

                s.getAiScore(),
                s.getAiFeedback(),

                s.getEvaluatedAt(),

                s.getCreatedAt(),
                s.getCreatedBy(),

                s.getUpdatedAt(),
                s.getUpdatedBy()
        );
    }
}