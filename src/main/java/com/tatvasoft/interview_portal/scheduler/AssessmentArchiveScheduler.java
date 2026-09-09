package com.tatvasoft.interview_portal.scheduler;
import com.tatvasoft.interview_portal.repository.AssessmentRepository;
import com.tatvasoft.interview_portal.entity.Assessment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
@Component
public class AssessmentArchiveScheduler {
    private static final Logger log = LoggerFactory.getLogger(AssessmentArchiveScheduler.class);
    private final AssessmentRepository assessmentRepository;
    public AssessmentArchiveScheduler(AssessmentRepository assessmentRepository) {
        this.assessmentRepository = assessmentRepository;
    }
    // Runs daily at 02:00 AM and archives assessments completed more than 6 months ago
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldCompletedAssessments() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
            // Fetch all assessments
            List<Assessment> allAssessments = assessmentRepository.findAll();
            log.info("AssessmentArchiveScheduler: fetched {} assessments for archiving check (cutoff={})", allAssessments.size(), cutoff);
            // Filter assessments that qualify for archiving
            List<Assessment> toArchiveAssessments = allAssessments.stream()
                    .filter(a -> a.getCompletedAt() != null
                            && a.getCompletedAt().isBefore(cutoff)
                            && Boolean.TRUE.equals(a.getIsActive()))
                    .toList();
            if (toArchiveAssessments.isEmpty()) {
                log.info("AssessmentArchiveScheduler: no assessments qualify for archiving.");
                return;
  }
            // Mark all qualifying assessments as inactive
            toArchiveAssessments.forEach(a -> a.setIsActive(false));
            // Persist all updates in a single batch operation
            assessmentRepository.saveAll(toArchiveAssessments);
            log.info("AssessmentArchiveScheduler: successfully archived {} assessment(s).", toArchiveAssessments.size());
        } catch (Exception e) {
            log.error("AssessmentArchiveScheduler: error occurred during archiving process.", e);
        }
    }
}
