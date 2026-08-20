package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Assessment;
import com.tatvasoft.interview_portal.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssessmentId(Long assessmentId);

}
