package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.ai.service.QuestionSelectionService;
import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.*;
import com.tatvasoft.interview_portal.repository.*;
import com.tatvasoft.interview_portal.service.QuestionService;
import com.tatvasoft.interview_portal.util.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionsRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final QuestionDesignationRepository questionDesignationRepository;
    private final QuestionSelectionService questionSelectionService;
    private final DataFormatter formatter = new DataFormatter();

    public QuestionServiceImpl(QuestionsRepository questionRepository, UserRepository userRepository, CategoryRepository categoryRepository, CandidateRepository candidateRepository, QuestionDesignationRepository questionDesignationRepository, QuestionSelectionService questionSelectionService) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.candidateRepository = candidateRepository;
        this.questionDesignationRepository = questionDesignationRepository;
        this.questionSelectionService = questionSelectionService;
    }

    @Override
    @Transactional
    public QuestionResponse addQuestion(QuestionRequest request) {

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged in user not found"));

        Question q = new Question();
        q.setTitle(request.getTitle());
        q.setDescription(request.getDescription());
        q.setDifficulty(request.getDifficulty());
        q.setEstimatedTime(request.getEstimatedTime());
        q.setIsActive(request.getIsActive());
        q.setCreatedBy(currentUser.getId());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Fetch all categories matching the IDs from the database
            java.util.List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

            // Convert the List to a Set (since your Question entity uses a Set for categories)
            q.setCategories(new java.util.HashSet<>(categories));
        }

        if (request.getDesignations() != null && !request.getDesignations().isEmpty()) {

            List<QuestionDesignation> designationEntities =
                    request.getDesignations()
                            .stream()
                            .map(designation -> {

                                QuestionDesignation qd = new QuestionDesignation();

                                qd.setDesignation(designation);
                                qd.setQuestion(q);
                                qd.setCreatedAt(LocalDateTime.now());

                                return qd;
                            })
                            .collect(Collectors.toList());

            q.setDesignations(designationEntities);
        }

        if (request.getSolutions() != null && !request.getSolutions().isEmpty()) {
            java.util.List<QuestionSolution> solutionEntities = request.getSolutions().stream().map(dto -> {
                QuestionSolution sol = new QuestionSolution();
                sol.setLanguage(dto.getLanguage());
                sol.setSolutionCode(dto.getSolutionCode());

                // 🔥 CRUCIAL: Set the parent question so the foreign key isn't null!
                sol.setQuestion(q);

                return sol;
            }).collect(java.util.stream.Collectors.toList());

            q.setSolutions(solutionEntities);
        }

        Question savedQuestion = questionRepository.save(q);

        return mapToQuestionResponse(savedQuestion);
    }

    @Override
    @Transactional
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(u -> new CategoryResponse(
                        u.getId(),
                        u.getName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse getQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        return mapToQuestionResponse(question);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {

        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        q.setTitle(request.getTitle());
        q.setDescription(request.getDescription());
        q.setDifficulty(request.getDifficulty());
        q.setEstimatedTime(request.getEstimatedTime());
        q.setIsActive(request.getIsActive());
        q.setUpdatedBy(currentUser.getId());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            java.util.List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            q.setCategories(new java.util.HashSet<>(categories));
        } else {
            q.getCategories().clear();
        }

        if (request.getSolutions() != null && !request.getSolutions().isEmpty()) {

            java.util.List<QuestionSolution> newSolutions = request.getSolutions().stream().map(dto -> {
                QuestionSolution sol = new QuestionSolution();
                sol.setLanguage(dto.getLanguage());
                sol.setSolutionCode(dto.getSolutionCode());
                sol.setQuestion(q);
                return sol;
            }).collect(java.util.stream.Collectors.toList());

            q.getSolutions().clear();
            q.getSolutions().addAll(newSolutions);

        } else {
            q.getSolutions().clear();
        }

        if (request.getDesignations() != null && !request.getDesignations().isEmpty()) {

            q.getDesignations().clear();

            questionRepository.saveAndFlush(q);

            List<QuestionDesignation> designationEntities =
                    request.getDesignations()
                            .stream()
                            .map(designation -> {

                                QuestionDesignation qd = new QuestionDesignation();

                                qd.setDesignation(designation);
                                qd.setQuestion(q);
                                qd.setCreatedAt(LocalDateTime.now());

                                return qd;
                            })
                            .collect(Collectors.toList());

            q.getDesignations().addAll(designationEntities);
        }

        Question updatedQuestion = questionRepository.saveAndFlush(q);

        return mapToQuestionResponse(updatedQuestion);
    }

    @Override
    public void deleteQuestion(Long id) {

        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }

        questionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<Question> uploadExcel(MultipartFile file) {

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged in user not found"));

        List<Question> uploaded = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Question question = new Question();

                question.setTitle(getCellValue(row.getCell(0)));
                question.setDescription(getCellValue(row.getCell(1)));
                question.setDifficulty(getCellValue(row.getCell(2)));

                question.setEstimatedTime(
                        Integer.parseInt(getCellValue(row.getCell(3)))
                );

                question.setIsActive(
                        Boolean.parseBoolean(getCellValue(row.getCell(4)))
                );

                question.setCreatedBy(currentUser.getId());

                // Categories
                String categoryCell = getCellValue(row.getCell(5));

                if (!categoryCell.isBlank()) {

                    List<Category> categories = Arrays.stream(categoryCell.split(","))
                            .map(String::trim)
                            .map(name ->
                                    categoryRepository
                                            .findByNameIgnoreCase(name)
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "Category not found : " + name)))
                            .toList();

                    question.setCategories(new HashSet<>(categories));
                }

                // Designations
                String designationCell = getCellValue(row.getCell(6));

                if (!designationCell.isBlank()) {

                    List<QuestionDesignation> designationList =
                            Arrays.stream(designationCell.split(","))
                                    .map(String::trim)
                                    .map(designation -> {

                                        QuestionDesignation qd =
                                                new QuestionDesignation();

                                        qd.setDesignation(designation);
                                        qd.setQuestion(question);
                                        qd.setCreatedAt(LocalDateTime.now());

                                        return qd;

                                    }).toList();

                    question.setDesignations(designationList);

                }

                // Java Solution
                String javaCode = getCellValue(row.getCell(7));

                if (!javaCode.isBlank()) {

                    QuestionSolution solution =
                            new QuestionSolution();

                    solution.setLanguage("JAVA");

                    solution.setSolutionCode(javaCode);

                    solution.setQuestion(question);

                    question.setSolutions(List.of(solution));

                }
                question.setCreatedBy(currentUser.getId());
                question.setCreatedAt(LocalDateTime.now());

                questionRepository.save(question);
            }

        } catch (Exception ex) {

            throw new RuntimeException(ex.getMessage());

        }

        return uploaded;
    }

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();

        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setDescription(question.getDescription());
        response.setDifficulty(question.getDifficulty());
        response.setEstimatedTime(question.getEstimatedTime());
        response.setIsActive(question.getIsActive());

        if (question.getCategories() != null) {
            response.setCategories(question.getCategories().stream()
                    .map(cat -> new CategoryDto(cat.getId(), cat.getName()))
                    .collect(Collectors.toList()));
        }

        if (question.getSolutions() != null) {
            response.setSolutions(question.getSolutions().stream()
                    .map(sol -> new SolutionDto(sol.getLanguage(), sol.getSolutionCode()))
                    .collect(Collectors.toList()));
        }

        if (question.getDesignations() != null) {
            response.setDesignations(question.getDesignations().stream()
                            .map(QuestionDesignation::getDesignation)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    @Override
    public List<QuestionResponse> recommendQuestions(
            Long candidateId,
            Integer maxMinutes) {

        Candidate candidate =
                candidateRepository.findById(candidateId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidate not found"
                                ));

        List<Question> availableQuestions =
                questionDesignationRepository
                        .findQuestionsByDesignation(
                                candidate.getDesignation()
                        );

        if (availableQuestions.isEmpty()) {

            throw new RuntimeException(
                    "No questions found for designation: "
                            + candidate.getDesignation()
            );
        }

        List<Long> selectedIds =
                questionSelectionService
                        .selectQuestions(
                                candidate.getDesignation(),
                                availableQuestions,
                                maxMinutes
                        );

        return availableQuestions.stream()
                .filter(q ->
                        selectedIds.contains(q.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    private QuestionResponse mapToResponse(
            Question question) {
        QuestionResponse response =
                new QuestionResponse();

        response.setId(question.getId());

        response.setTitle(question.getTitle());

        response.setDescription(question.getDescription());

        response.setDifficulty(question.getDifficulty());

        response.setEstimatedTime(question.getEstimatedTime());

        response.setIsActive(question.getIsActive());

        response.setDesignations(
                question.getDesignations()
                        .stream()
                        .map(QuestionDesignation::getDesignation)
                        .toList()
        );

        return response;
    }
}