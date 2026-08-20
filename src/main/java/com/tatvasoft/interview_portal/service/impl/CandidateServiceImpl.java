package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.ai.dto.MultiQuestionEvaluationResult;
import com.tatvasoft.interview_portal.ai.dto.QuestionEvaluationResult;
import com.tatvasoft.interview_portal.dto.CandidateEvaluationResponse;
import com.tatvasoft.interview_portal.dto.CandidateRequest;
import com.tatvasoft.interview_portal.dto.CandidateResponse;
import com.tatvasoft.interview_portal.dto.QuestionUploadDto;
import com.tatvasoft.interview_portal.entity.*;
import com.tatvasoft.interview_portal.exception.ResourceNotFoundException;
import com.tatvasoft.interview_portal.repository.*;
import com.tatvasoft.interview_portal.service.CandidateService;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final QuestionsRepository questionsRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final CandidateSolutionRepository candidateSolutionRepository;
    private final QuestionSolutionRepository questionSolutionRepository;

    public CandidateServiceImpl(CandidateRepository candidateRepository, UserRepository userRepository, SubmissionRepository submissionRepository, AssessmentRepository assessmentRepository, QuestionsRepository questionsRepository, AssessmentQuestionRepository assessmentQuestionRepository, CandidateSolutionRepository candidateSolutionRepository, QuestionSolutionRepository questionSolutionRepository) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.assessmentRepository = assessmentRepository;
        this.questionsRepository = questionsRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.candidateSolutionRepository = candidateSolutionRepository;
        this.questionSolutionRepository = questionSolutionRepository;
    }

    @Override
    public CandidateResponse create(CandidateRequest request) {

        if (candidateRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Candidate email already exists");
        }

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Logged in user not found"));

        Candidate candidate = new Candidate();

        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setExperience(request.getExperience());
        candidate.setDesignation(request.getDesignation());
        candidate.setIsActive(request.getIsActive());

        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setCreatedBy(currentUser.getId());

        Candidate saved = candidateRepository.save(candidate);

        return map(saved);
    }

    @Override
    public List<CandidateResponse> getAll() {

        return candidateRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    @Override
    public CandidateResponse getById(Long id) {

        Candidate candidate = candidateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        return map(candidate);
    }

    @Override
    public CandidateResponse update(Long id, CandidateRequest request) {

        Candidate candidate = candidateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Logged in user not found"));

        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
        candidate.setExperience(request.getExperience());
        candidate.setDesignation(request.getDesignation());
        candidate.setIsActive(request.getIsActive());
        candidate.setUpdatedAt(LocalDateTime.now());
        candidate.setUpdatedBy(currentUser.getId());

        Candidate updated = candidateRepository.save(candidate);

        return map(updated);
    }

    @Override
    public void delete(Long id) {

        Candidate candidate = candidateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        candidateRepository.delete(candidate);
    }

    private CandidateResponse map(Candidate candidate) {

        return new CandidateResponse(candidate.getId(), candidate.getFirstName(), candidate.getLastName(), candidate.getEmail(), candidate.getExperience(), candidate.getDesignation(), candidate.getIsActive());
    }

    @Override
    public CandidateEvaluationResponse getCandidateEvaluation(Long candidateId) {

        Assessment assessment = assessmentRepository.findByCandidateId(candidateId).orElseThrow();

        CandidateEvaluationResponse response = new CandidateEvaluationResponse();

        response.setAssessmentId(assessment.getId());

        response.setAssessmentTitle(assessment.getTitle());

        Submission submission = submissionRepository.findByAssessmentId(assessment.getId()).orElse(null);

        if (submission != null) {

            response.setIsEvaluated(true);

            response.setEvaluation(buildEvaluationResult(assessment.getId()));

            return response;
        }

        response.setIsEvaluated(false);

        List<QuestionUploadDto> questions = loadQuestions((Math.toIntExact(assessment.getId())));

        response.setQuestions(questions);

        return response;
    }

    private MultiQuestionEvaluationResult buildEvaluationResult(Long assessmentId) {

        MultiQuestionEvaluationResult result = new MultiQuestionEvaluationResult();

        Submission submission = submissionRepository.findByAssessmentId(assessmentId).orElse(null);

        List<CandidateSolution> solutions = candidateSolutionRepository.findBySubmissionId(submission.getId());

        List<QuestionEvaluationResult> evaluations = new ArrayList<>();

        int questionNumber = 1;

        for (CandidateSolution s : solutions) {

            QuestionEvaluationResult q =
                    new QuestionEvaluationResult();

            Question question =
                    questionsRepository
                            .findById(s.getQuestionId())
                            .orElse(null);

            q.setQuestionId(s.getQuestionId());

            q.setQuestionNumber(questionNumber++);

            if (question != null) {

                q.setQuestionTopic(
                        question.getTitle()
                );
            }

            q.setCandidateCode(
                    s.getSolution()
            );

            q.setScore(
                    s.getAiScore()
            );

            q.setFeedback(
                    s.getAiFeedback()
            );

            q.setTimeComplexity(
                    s.getTimeComplexity()
            );

            q.setSpaceComplexity(
                    s.getSpaceComplexity()
            );

            q.setOptimizedCode(
                    s.getOptimizedCode()
            );

            q.setMissedEdgeCases(
                    parseList(
                            s.getMissedEdgeCases()
                    )
            );

            q.setSecurityIssues(
                    parseList(
                            s.getSecurityIssues()
                    )
            );

            evaluations.add(q);
        }

        result.setIsSuccess(true);

        result.setEvaluations(evaluations);

        result.setTotalQuestions(evaluations.size());

        result.setOverallScore((double) submission.getAiScore());

        return result;
    }
    private List<String> parseList(String value) {

        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(
                        value
                                .replace("[", "")
                                .replace("]", "")
                                .split(",")
                )
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    private List<QuestionUploadDto> loadQuestions(Integer assessmentId) {

        List<AssessmentQuestion> links = assessmentQuestionRepository.findByAssessmentId(assessmentId);

        List<QuestionUploadDto> questions = new ArrayList<>();

        for (AssessmentQuestion link : links) {

            Question question = questionsRepository.findById(Long.valueOf(link.getQuestionId())).orElseThrow();

            QuestionUploadDto dto = new QuestionUploadDto();

            dto.setQuestionId(question.getId());

            dto.setQuestionTitle(question.getTitle());

            dto.setQuestionDescription(question.getDescription());
            QuestionSolution solution = questionSolutionRepository.findByQuestionIdAndIsActiveTrue(question.getId()).orElse(null);

            if (solution != null) {

                dto.setSolutionFileId(solution.getId());

                dto.setSolutionFileName(question.getTitle().replace(" ", ""));
            }

            questions.add(dto);
        }

        return questions;
    }
}