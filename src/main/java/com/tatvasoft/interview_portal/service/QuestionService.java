package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.dto.CategoryResponse;
import com.tatvasoft.interview_portal.dto.QuestionRequest;
import com.tatvasoft.interview_portal.dto.QuestionResponse;
import com.tatvasoft.interview_portal.entity.Question;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionService {

    QuestionResponse addQuestion(QuestionRequest request);

    List<QuestionResponse> getAllQuestions();

    List<CategoryResponse> getAllCategories();

    QuestionResponse getQuestion(Long id);

    QuestionResponse updateQuestion(Long id, QuestionRequest request);

    void deleteQuestion(Long id);

    List<Question> uploadExcel(MultipartFile file);

    List<QuestionResponse> recommendQuestions(
            Long candidateId,
            Integer maxMinutes
    );
}