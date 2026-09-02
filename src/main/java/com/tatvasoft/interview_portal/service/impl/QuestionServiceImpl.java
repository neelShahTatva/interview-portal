package com.tatvasoft.interview_portal.service.impl;

import com.tatvasoft.interview_portal.ai.service.QuestionSelectionService;
import com.tatvasoft.interview_portal.dto.*;
import com.tatvasoft.interview_portal.entity.*;
import com.tatvasoft.interview_portal.enums.QuestionDesignationType;
import com.tatvasoft.interview_portal.enums.QuestionDifficultyLevel;
import com.tatvasoft.interview_portal.enums.QuestionUploadHeader;
import com.tatvasoft.interview_portal.exception.BulkUploadValidationException;
import com.tatvasoft.interview_portal.repository.*;
import com.tatvasoft.interview_portal.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
import com.tatvasoft.interview_portal.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionsRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionDesignationRepository questionDesignationRepository;
    private final QuestionSolutionRepository questionSolutionRepository;
    private final QuestionSelectionService questionSelectionService;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    private final DataFormatter formatter = new DataFormatter();

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> new CategoryResponse(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse addQuestion(QuestionRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        Question q = new Question();
        q.setTitle(request.getTitle());
        q.setDescription(request.getDescription());
        q.setDifficulty(request.getDifficulty());
        q.setEstimatedTime(request.getEstimatedTime());
        q.setIsActive(request.getIsActive());
        q.setCreatedBy(currentUser.getId());
        q.setCreatedAt(LocalDateTime.now());

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            q.setCategories(new HashSet<>(categories));
        }

        if (request.getSolutions() != null && !request.getSolutions().isEmpty()) {
            List<QuestionSolution> sols = request.getSolutions().stream().map(dto -> {
                QuestionSolution sol = new QuestionSolution();
                sol.setLanguage(dto.getLanguage());
                sol.setSolutionCode(dto.getSolutionCode());
                sol.setQuestion(q);
                return sol;
            }).collect(Collectors.toList());
            q.setSolutions(sols);
        }

        if (request.getDesignations() != null && !request.getDesignations().isEmpty()) {
            List<QuestionDesignation> designations = request.getDesignations().stream().map(d -> {
                QuestionDesignation qd = new QuestionDesignation();
                qd.setDesignation(d);
                qd.setQuestion(q);
                qd.setCreatedAt(LocalDateTime.now());
                return qd;
            }).collect(Collectors.toList());
            q.setDesignations(designations);
        }

        Question saved = questionRepository.save(q);
        return mapToQuestionResponse(saved);
    }
    
    @Override
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
    public QuestionResponse getQuestion(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return mapToQuestionResponse(q);
    }

    @Override
    @Transactional
    public List<Question> uploadExcel(MultipartFile file) {

        log.info("Starting bulk upload: filename={}", file != null ? file.getOriginalFilename() : "<null>");

        if (file == null || file.isEmpty()) {
            throw new BulkUploadValidationException("File is required.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !(originalFileName.toLowerCase().endsWith(".xls") || originalFileName.toLowerCase().endsWith(".xlsx"))) {
            throw new BulkUploadValidationException("Invalid file type. Please upload a .xls or .xlsx file.");
        }

        String username = SecurityUtil.getCurrentUsername();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Logged in user not found"));

        List<Question> uploaded = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new BulkUploadValidationException("Excel file is missing the required sheet.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() < 1) {
                throw new BulkUploadValidationException("Uploaded file is empty.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BulkUploadValidationException("Excel file is missing required columns: Title, Description, Difficulty, Estimated Time, Is Active, Categories, Designations, Java Solution.");
            }

            String[] requiredHeaders = QuestionUploadHeader.getRequiredHeaders();

            // Build header -> column index map

            Map<String, Integer> rawHeaderIndex = new HashMap<>();
            for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = getCellValue(cell);
                    if (!headerName.isBlank()) {
                        rawHeaderIndex.put(normalizeHeader(headerName), i);
                    }
                }
            }

            // Try to map required headers with fuzzy/alias matching so uploads tolerate small header variations
            Map<String, Integer> columnIndexByHeader = buildFuzzyHeaderMap(rawHeaderIndex);

            // Collect missing headers but do not abort; we'll report header errors along with per-row errors
            List<String> missingHeaders = getMissingHeaders(columnIndexByHeader);
            if (!missingHeaders.isEmpty()) {
                errors.add("Excel file is missing required columns: " + String.join(", ", missingHeaders) + ".");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                int rowNumber = i + 1;

                int titleIndex = getOptionalColumnIndex(columnIndexByHeader, "title");
                int descriptionIndex = getOptionalColumnIndex(columnIndexByHeader, "description");
                int difficultyIndex = getOptionalColumnIndex(columnIndexByHeader, "difficulty");
                int estimatedTimeIndex = getOptionalColumnIndex(columnIndexByHeader, "estimatedtime");
                int isActiveIndex = getOptionalColumnIndex(columnIndexByHeader, "isactive");
                int categoryIndex = getOptionalColumnIndex(columnIndexByHeader, "categories");
                int designationIndex = getOptionalColumnIndex(columnIndexByHeader, "designations");
                int solutionIndex = getOptionalColumnIndex(columnIndexByHeader, "javasolution");

                boolean hasTitleCol = titleIndex >= 0;
                boolean hasDescriptionCol = descriptionIndex >= 0;
                boolean hasDifficultyCol = difficultyIndex >= 0;
                boolean hasEstimatedTimeCol = estimatedTimeIndex >= 0;
                boolean hasIsActiveCol = isActiveIndex >= 0;
                boolean hasCategoryCol = categoryIndex >= 0;
                boolean hasDesignationCol = designationIndex >= 0;
                boolean hasSolutionCol = solutionIndex >= 0;

                String title = safeGetCellValue(row, titleIndex);
                String description = safeGetCellValue(row, descriptionIndex);
                String difficulty = safeGetCellValue(row, difficultyIndex);
                String estimatedTimeText = safeGetCellValue(row, estimatedTimeIndex);
                String isActiveText = safeGetCellValue(row, isActiveIndex);
                String categoryCell = safeGetCellValue(row, categoryIndex);
                String designationCell = safeGetCellValue(row, designationIndex);
                String javaCode = safeGetCellValue(row, solutionIndex);

                if (hasTitleCol) {
                    if (title.isBlank()) {
                        errors.add("Row " + rowNumber + ": Title is required.");
                    }
                }

                if (hasDescriptionCol) {
                    if (description.isBlank()) {
                        errors.add("Row " + rowNumber + ": Description is required.");
                    }
                }

                if (hasDifficultyCol) {
                    if (difficulty.isBlank()) {
                        errors.add("Row " + rowNumber + ": Difficulty is required.");
                    } else if (!isValidDifficulty(difficulty)) {
                        errors.add("Row " + rowNumber + ": Difficulty must be EASY, MEDIUM, or HARD.");
                    }
                }

                if (hasEstimatedTimeCol) {
                    if (estimatedTimeText.isBlank()) {
                        errors.add("Row " + rowNumber + ": Estimated Time is required and must be a positive number.");
                    } else {
                        try {
                            int estimatedTime = Integer.parseInt(estimatedTimeText.trim());
                            if (estimatedTime <= 0) {
                                errors.add("Row " + rowNumber + ": Estimated Time is required and must be a positive number.");
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Row " + rowNumber + ": Estimated Time is required and must be a positive number.");
                        }
                    }
                }

                String normalizedIsActive = normalizeBooleanValue(isActiveText);
                if (hasIsActiveCol) {
                    if (normalizedIsActive == null) {
                        errors.add("Row " + rowNumber + ": Is Active must be TRUE or FALSE.");
                    }
                }

                if (hasCategoryCol && !categoryCell.isBlank()) {
                    String[] categoryNames = categoryCell.split(",");
                    for (String categoryName : categoryNames) {
                        String trimmed = categoryName.trim();
                        if (!trimmed.isEmpty()) {
                            boolean exists = categoryRepository.findByNameIgnoreCase(trimmed).isPresent();
                            if (!exists) {
                                errors.add("Row " + rowNumber + ": Category not found: " + trimmed + ".");
                            }
                        }
                    }
                }

                if (hasDesignationCol && !designationCell.isBlank()) {
                    String[] designations = designationCell.split(",");
                    List<String> invalidDesignations = Arrays.stream(designations)
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .filter(value -> !isValidDesignation(value))
                            .toList();

                    if (invalidDesignations.size() > 0) {
                        errors.add("Row " + rowNumber + ": Invalid designation(s): " + String.join(", ", invalidDesignations)
                                + ". Valid designations are: TSE, ASE, SE, SSE, TL, STL, APM, PM, PPM.");
                    }
                } else if (hasDesignationCol) {
                    errors.add("Row " + rowNumber + ": Designation is required.");
                }

                if (hasSolutionCol) {
                    if (javaCode.isBlank()) {
                        errors.add("Row " + rowNumber + ": Java Solution is required.");
                    } else if (javaCode.length() < 5) {
                        errors.add("Row " + rowNumber + ": Java solution code is invalid or empty.");
                    }
                }

                if (errors.size() > 0 && errors.stream().anyMatch(msg -> msg.startsWith("Row " + rowNumber + ":"))) {
                    continue;
                }

                Question question = new Question();
                question.setTitle(title);
                question.setDescription(description);
                question.setDifficulty(difficulty);
                question.setEstimatedTime(Integer.parseInt(estimatedTimeText.trim()));
                question.setIsActive(Boolean.parseBoolean(normalizedIsActive));
                question.setCreatedBy(currentUser.getId());

                String[] categoryNames = categoryCell.split(",");
                if (!categoryCell.isBlank()) {
                    List<Category> categories = Arrays.stream(categoryNames)
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .map(name -> categoryRepository.findByNameIgnoreCase(name)
                                    .orElseThrow(() -> new BulkUploadValidationException(
                                            "Row " + rowNumber + ": Category not found: " + name + ".")))
                            .toList();
                    question.setCategories(new HashSet<>(categories));
                }

                String designationValue = getCellValue(row.getCell(designationIndex));
                if (!designationValue.isBlank()) {
                    List<QuestionDesignation> designationList = Arrays.stream(designationValue.split(","))
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .map(designation -> {
                                QuestionDesignation qd = new QuestionDesignation();
                                qd.setDesignation(designation);
                                qd.setQuestion(question);
                                qd.setCreatedAt(LocalDateTime.now());
                                return qd;
                            }).toList();
                    question.setDesignations(designationList);
                }

                String javaValue = getCellValue(row.getCell(solutionIndex));
                if (StringUtils.isNotBlank(javaValue)   ) {
                    QuestionSolution solution = new QuestionSolution();
                    solution.setLanguage("JAVA");
                    solution.setSolutionCode(javaValue);
                    solution.setQuestion(question);
                    question.setSolutions(List.of(solution));
                }

                question.setCreatedAt(LocalDateTime.now());
                questionRepository.save(question);
                uploaded.add(question);
            }

            if (!errors.isEmpty()) {
                throw new BulkUploadValidationException(String.join(" | ", errors));
            }

        } catch (BulkUploadValidationException ex) {
            log.warn("Bulk upload validation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Bulk upload encountered an error", ex);
            throw new BulkUploadValidationException("Bulk upload failed: " + ex.getMessage(), ex);
        }

        return uploaded;
    }

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private String normalizeBooleanValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String cleaned = trimmed.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        if (cleaned.startsWith("true")) {
            return "true";
        }
        if (cleaned.startsWith("false")) {
            return "false";
        }
        return null;
    }

    private boolean isValidDesignation(String designation) {
        if (designation == null) {
            return false;
        }

        String normalized = designation.trim().toUpperCase(Locale.ROOT);
        return QuestionDesignationType.isValid(designation);
    }

    private boolean isValidDifficulty(String difficulty) {
        return QuestionDifficultyLevel.isValid(difficulty);
    }

    private boolean headersMatch(String requiredHeader, String actualHeader) {
        if (requiredHeader == null || actualHeader == null) {
            return false;
        }

        String normalizedRequired = normalizeHeader(requiredHeader);
        String normalizedActual = normalizeHeader(actualHeader);

        if (normalizedRequired.equals(normalizedActual)) {
            return true;
        }

        Set<String> aliases = new HashSet<>();
        aliases.add("estimatedtime");
        aliases.add("isactive");
        aliases.add("javasolution");
        aliases.add("solutioncode");

        return aliases.contains(normalizedRequired) && aliases.contains(normalizedActual);
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private void validateRequiredHeaders(Map<String, Integer> columnIndexByHeader) {
        String[] requiredHeaders = QuestionUploadHeader.getRequiredHeaders();

        List<String> missingHeaders = Arrays.stream(requiredHeaders)
                .map(QuestionUploadHeader::normalizeHeader)
                .filter(header -> !columnIndexByHeader.containsKey(header))
                .map(header -> switch (header) {
                        case "title" -> "Title";
                        case "description" -> "Description";
                        case "difficulty" -> "Difficulty";
                        case "estimatedtime" -> "Estimated Time";
                        case "isactive" -> "Is Active";
                        case "categories" -> "Categories";
                        case "designations" -> "Designations";
                        case "javasolution" -> "Java Solution";
                        default -> header;
                })
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new BulkUploadValidationException(
                    "Excel file is missing required columns: " + String.join(", ", missingHeaders) + "."
            );
        }
    }

    private List<String> getMissingHeaders(Map<String, Integer> columnIndexByHeader) {
        String[] requiredHeaders = QuestionUploadHeader.getRequiredHeaders();

        return Arrays.stream(requiredHeaders)
                .map(QuestionUploadHeader::normalizeHeader)
                .filter(header -> !columnIndexByHeader.containsKey(header))
                .map(header -> switch (header) {
                    case "title" -> "Title";
                    case "description" -> "Description";
                    case "difficulty" -> "Difficulty";
                    case "estimatedtime" -> "Estimated Time";
                    case "isactive" -> "Is Active";
                    case "categories" -> "Categories";
                    case "designations" -> "Designations";
                    case "javasolution" -> "Java Solution";
                    default -> header;
                })
                .toList();
    }

    private int getOptionalColumnIndex(Map<String, Integer> columnIndexByHeader, String headerKey) {
        return columnIndexByHeader.getOrDefault(normalizeHeader(headerKey), -1);
    }

    private String safeGetCellValue(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) return "";
        Cell cell = row.getCell(columnIndex);
        return getCellValue(cell);
    }

    private Map<String, Integer> buildFuzzyHeaderMap(Map<String, Integer> rawHeaderIndex) {
        String[] requiredHeaders = QuestionUploadHeader.getRequiredHeaders();

        Map<String, Integer> result = new HashMap<>();

        // Aliases for certain headers
        Map<String, List<String>> aliases = Map.of(
                "isactive", List.of("isactive", "active", "status"),
                "estimatedtime", List.of("estimatedtime", "estimated", "time", "estimated_time"),
                "javasolution", List.of("javasolution", "solution", "solutioncode", "java_solution")
        );

        // First try exact normalized matches
        for (String h : requiredHeaders) {
            String norm = normalizeHeader(h);
            if (rawHeaderIndex.containsKey(norm)) {
                result.put(norm, rawHeaderIndex.get(norm));
            }
        }

        // Try aliases
        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            String target = entry.getKey();
            if (result.containsKey(target)) continue;
            for (String a : entry.getValue()) {
                if (rawHeaderIndex.containsKey(a)) {
                    result.put(target, rawHeaderIndex.get(a));
                    break;
                }
            }
        }

        // Try contains/substring matching for remaining headers
        for (String h : requiredHeaders) {
            String norm = normalizeHeader(h);
            if (result.containsKey(norm)) continue;
            for (Map.Entry<String, Integer> r : rawHeaderIndex.entrySet()) {
                String raw = r.getKey();
                if (raw.contains(norm) || norm.contains(raw) || raw.startsWith(norm) || norm.startsWith(raw)) {
                    result.put(norm, r.getValue());
                    break;
                }
            }
        }

        return result;
    }

    private int getRequiredColumnIndex(Map<String, Integer> columnIndexByHeader, String headerKey) {
        Integer index = columnIndexByHeader.get(normalizeHeader(headerKey));
        if (index == null) {
            throw new BulkUploadValidationException(
                    "Excel file is missing required columns: " + headerKey + "."
            );
        }
        return index;
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