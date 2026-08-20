package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Question;
import com.tatvasoft.interview_portal.entity.QuestionDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionDesignationRepository extends JpaRepository<QuestionDesignation, Long> {
    @Query("""
        SELECT DISTINCT qd.question
        FROM QuestionDesignation qd
        WHERE LOWER(qd.designation)
                =
              LOWER(:designation)
        AND qd.question.isActive = true
    """)
    List<Question> findQuestionsByDesignation(
            String designation
    );
}
