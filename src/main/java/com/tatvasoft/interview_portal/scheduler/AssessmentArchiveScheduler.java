package com.tatvasoft.interview_portal.scheduler;

import com.tatvasoft.interview_portal.service.AssessmentArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssessmentArchiveScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(AssessmentArchiveScheduler.class);

    private final AssessmentArchiveService assessmentArchiveService;

    public AssessmentArchiveScheduler(
            AssessmentArchiveService assessmentArchiveService) {
        this.assessmentArchiveService = assessmentArchiveService;
    }

    // Runs daily at 02:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldCompletedAssessments() {

        log.info("AssessmentArchiveScheduler: starting scheduled archive process.");

        try {
            assessmentArchiveService.archiveOldCompletedAssessments();

            log.info(
                    "AssessmentArchiveScheduler: scheduled archive process completed successfully."
            );

        } catch (Exception e) {
            log.error(
                    "AssessmentArchiveScheduler: error occurred while executing archive process.",
                    e
            );
        }
    }
}