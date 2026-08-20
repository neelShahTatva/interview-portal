package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Integer> {

    List<AssessmentQuestion> findByAssessmentId(Integer assessmentId);

    void deleteByAssessmentId(Integer assessmentId);
}