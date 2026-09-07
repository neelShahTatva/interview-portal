package com.tatvasoft.interview_portal.scheduler;

import com.tatvasoft.interview_portal.repository.AssessmentRepository;
import com.tatvasoft.interview_portal.entity.Assessment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AssessmentArchiveScheduler {

    private final AssessmentRepository assessmentRepository;

    public AssessmentArchiveScheduler(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }

    // Runs daily at 02:00 AM and archives assessments completed more than 6 months ago
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldCompletedAssessments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);

        List<Assessment> all = assessmentRepository.findAll();

        for (Assessment a : all) {
            if (a.getCompletedAt() != null && a.getCompletedAt().isBefore(cutoff) && Boolean.TRUE.equals(a.getIsActive())) {
                a.setIsActive(false);
                assessmentRepository.save(a);
            }
        }
    }
}
