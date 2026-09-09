package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.*;
import com.tatvasoft.interview_portal.enums.AssessmentStatus;
import com.tatvasoft.interview_portal.repository.*;
import com.tatvasoft.interview_portal.service.AssessmentService;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository repository;
    private final UserRepository userRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final QuestionsRepository questionsRepository;
    private final CandidateRepository candidateRepository;

    public AssessmentServiceImpl(AssessmentRepository repository, UserRepository userRepository, AssessmentQuestionRepository assessmentQuestionRepository, QuestionsRepository questionsRepository, CandidateRepository candidateRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.questionsRepository = questionsRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    @Transactional
    public AssessmentResponse create(AssessmentRequest request) {

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
                // Find active assessments for the candidate and decide eligibility
    List<Assessment> activeForCandidate =
        repository.findAllByCandidateId(request.getCandidateId())
                .stream()
                .filter(Assessment::getIsActive)
                .toList();

if (!activeForCandidate.isEmpty()) {

    boolean hasPending = activeForCandidate.stream()
            .anyMatch(a -> AssessmentStatus.PENDING.name().equals(a.getStatus()));

    if (hasPending) {
        throw new RuntimeException(
                "Candidate already has a pending assessment."
        );
    }

    boolean hasInProgress = activeForCandidate.stream()
            .anyMatch(a -> AssessmentStatus.IN_PROGRESS.name().equals(a.getStatus()));

    if (hasInProgress) {
        throw new RuntimeException(
                "Candidate already has an assessment in progress."
        );
    }

    Assessment latestCompleted = activeForCandidate.stream()
            .filter(a -> AssessmentStatus.COMPLETED.name().equals(a.getStatus()))
            .filter(a -> a.getCompletedAt() != null)
            .max(Comparator.comparing(Assessment::getCompletedAt))
            .orElse(null);

    if (latestCompleted != null) {

        LocalDateTime eligibleDate =
                latestCompleted.getCompletedAt().plusMonths(6);

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(eligibleDate)) {

            Period period = Period.between(
                    now.toLocalDate(),
                    eligibleDate.toLocalDate()
            );

            int months = period.getMonths();
            int days = period.getDays();

            StringBuilder message = new StringBuilder(
                    "Candidate is not eligible for a new assessment. You will be eligible after "
            );

                        if (months > 0) {
                                message.append(months)
                                                .append(months == 1 ? " month" : " months");
                        }

                        if (days > 0) {
                                if (months > 0) {
                                        message.append(" and ");
                                }

                                message.append(days)
                                                .append(days == 1 ? " day" : " days");
                        }

                        // If both months and days are zero (same target date but later time),
                        // show hours/minutes remaining or the exact eligible date/time.
                        if (months == 0 && days == 0) {
                                long hours = ChronoUnit.HOURS.between(now, eligibleDate);
                                long minutes = ChronoUnit.MINUTES.between(now.plusHours(hours), eligibleDate);

                                if (hours > 0) {
                                        if (months > 0 || days > 0) message.append(" and ");
                                        message.append(hours).append(hours == 1 ? " hour" : " hours");

                                        if (minutes > 0) {
                                                message.append(" and ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
                                        }
                                } else if (minutes > 0) {
                                        if (months > 0 || days > 0) message.append(" and ");
                                        message.append(minutes).append(minutes == 1 ? " minute" : " minutes");
                                } else {
                                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                        if (months > 0 || days > 0) message.append(" ");
                                        message.append(eligibleDate.format(fmt));
                                }
                        }

                        message.append(".");

            throw new RuntimeException(message.toString());
        }
    }
}

                      
        Assessment assessment = new Assessment();

        assessment.setCandidateId(request.getCandidateId());
        assessment.setTitle(request.getTitle());
        assessment.setTimeLimitMinutes(request.getTimeLimitMinutes());

        assessment.setStatus(AssessmentStatus.PENDING.name());

        assessment.setIsActive(true);

        assessment.setCreatedBy(currentUser.getId());

        assessment.setCreatedAt(LocalDateTime.now());

        Assessment savedAssessment = repository.save(assessment);

        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {

            List<AssessmentQuestion> mappings = request.getQuestionIds().stream().map(questionId -> {

                AssessmentQuestion aq = new AssessmentQuestion();

                aq.setAssessmentId(savedAssessment.getId().intValue());

                aq.setQuestionId(questionId.intValue());

                aq.setCreatedAt(LocalDateTime.now());

                aq.setCreatedBy(currentUser.getId().intValue());

                return aq;

            }).toList();

            assessmentQuestionRepository.saveAll(mappings);
        }

        return map(savedAssessment);
    }

    @Override
    public List<AssessmentResponse> getAll() {
        return repository.findAll()
                .stream()
                .filter(
                        Assessment::getIsActive
                )
                .map(this::map)
                .toList();
    }

    @Override
    public AssessmentResponse getById(Long id) {
        Assessment a = repository.findById(id).orElseThrow(() -> new RuntimeException("Assessment not found"));

        return map(a);
    }

//    @Override
//    public AssessmentResponse update(Long id, AssessmentRequest request) {
//
//        String username = SecurityUtil.getCurrentUsername();
//
//        User currentUser = userRepository.findByUsername(username)
//                .orElseThrow(() ->
//                        new RuntimeException("Logged in user not found"));
//
//        Assessment a = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Assessment not found"));
//
//        a.setCandidateId(request.getCandidateId());
//        a.setTimeLimitMinutes(request.getTimeLimitMinutes());
//        a.setStatus(request.getStatus());
//
//        a.setStartedAt(request.getStartedAt());
//        a.setCompletedAt(request.getCompletedAt());
//
//        a.setUpdatedBy(currentUser.getId());
//        a.setUpdatedAt(LocalDateTime.now());
//
//        return map(repository.save(a));
//    }

    @Override
    @Transactional
    public void delete(Long id) {

        Assessment assessment =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assessment not found"));

        assessment.setIsActive(false);

        assessment.setUpdatedAt(
                LocalDateTime.now()
        );

        assessment.setUpdatedBy(1L);

        repository.save(assessment);

        assessmentQuestionRepository
                .deleteByAssessmentId(
                        id.intValue()
                );

    }

    private AssessmentResponse map(
            Assessment assessment) {

        List<AssessmentQuestion> mappings =
                assessmentQuestionRepository
                        .findByAssessmentId(
                                assessment.getId()
                                        .intValue()
                        );

        List<Long> questionIds =
                mappings.stream()
                        .map(x ->
                                x.getQuestionId()
                                        .longValue())
                        .toList();

        List<QuestionResponse> questions =
                questionsRepository
                        .findByIdIn(questionIds)
                        .stream()
                        .map(this::mapQuestion)
                        .toList();

        return new AssessmentResponse(

                assessment.getId(),

                assessment.getCandidateId(),

                assessment.getStatus(),

                assessment.getTimeLimitMinutes(),

                assessment.getIsActive(),

                assessment.getTitle(),

                questions,

                assessment.getStartedAt(),

                assessment.getCompletedAt(),

                assessment.getCreatedAt(),

                assessment.getCreatedBy(),

                assessment.getUpdatedAt(),

                assessment.getUpdatedBy()
        );
    }

    private QuestionResponse mapQuestion(
            Question question) {

        return new QuestionResponse(

                question.getId(),

                question.getTitle(),

                question.getDescription(),

                question.getDesignations()
                        .stream()
                        .map(
                                QuestionDesignation::getDesignation
                        )
                        .toList(),

                question.getDifficulty(),

                question.getEstimatedTime(),

                question.getIsActive(),

                question.getCategories()
                        .stream()
                        .map(c ->
                                new CategoryDto(
                                        c.getId(),
                                        c.getName()))
                        .toList(),

                question.getSolutions()
                        .stream()
                        .map(s ->
                                new SolutionDto(
                                        s.getLanguage(),
                                        s.getSolutionCode()))
                        .toList()
        );
    }

    @Override
    public void changeStatus(Long id, String status) {

        Assessment assessment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        LocalDateTime now = LocalDateTime.now();

        assessment.setStatus(status);
        assessment.setUpdatedAt(now);

        if (AssessmentStatus.COMPLETED.name().equalsIgnoreCase(status)) {
            assessment.setCompletedAt(now);
        }

        repository.save(assessment);
    }

    @Override
    public List<CandidateResponse> getAvailableCandidates() {

        List<Long> assignedCandidateIds =
                repository.findByIsActiveTrue()
                        .stream()
                        .map(Assessment::getCandidateId)
                        .toList();

        return candidateRepository.findAll()
                .stream()
                .filter(candidate ->
                        !assignedCandidateIds.contains(
                                candidate.getId()))
                .map(this::mapCandidate)
                .toList();
    }

    private CandidateResponse mapCandidate(Candidate candidate) {

        return new CandidateResponse(candidate.getId(), candidate.getFirstName(), candidate.getLastName(), candidate.getEmail(), candidate.getExperience(), candidate.getDesignation(), candidate.getIsActive());
    }
}
