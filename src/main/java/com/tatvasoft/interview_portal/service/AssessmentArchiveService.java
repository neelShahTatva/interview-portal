package com.tatvasoft.interview_portal.service;

import com.tatvasoft.interview_portal.entity.Assessment;
import com.tatvasoft.interview_portal.repository.AssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssessmentArchiveService {

    private static final Logger log =
            LoggerFactory.getLogger(AssessmentArchiveService.class);

    private final AssessmentRepository assessmentRepository;

    public AssessmentArchiveService(
            AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional
    public void archiveOldCompletedAssessments() {

        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);

        log.info(
                "AssessmentArchiveService: starting database archive operation. Cutoff={}",
                cutoff
        );

        // Fetch only active assessments completed more than 6 months ago
        List<Assessment> assessmentsToArchive =
                assessmentRepository.findByCompletedAtBeforeAndIsActiveTrue(
                        cutoff
                );

        log.info(
                "AssessmentArchiveService: found {} assessment(s) eligible for archiving.",
                assessmentsToArchive.size()
        );

        if (assessmentsToArchive.isEmpty()) {
            log.info(
                    "AssessmentArchiveService: no assessments qualify for archiving."
            );
            return;
        }

        // Mark qualifying assessments as inactive
        assessmentsToArchive.forEach(
                assessment -> assessment.setIsActive(false)
        );

        // Persist the changes
        assessmentRepository.saveAll(assessmentsToArchive);

        log.info(
                "AssessmentArchiveService: successfully archived {} assessment(s).",
                assessmentsToArchive.size()
        );
    }
}