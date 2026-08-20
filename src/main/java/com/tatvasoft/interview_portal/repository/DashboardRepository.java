package com.tatvasoft.interview_portal.repository;

import com.tatvasoft.interview_portal.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Candidate, Long> {

    // Stats

    @Query(value = "SELECT COUNT(*) FROM candidates", nativeQuery = true)
    long countTotalCandidates();

    @Query(value = "SELECT COUNT(*) FROM candidates WHERE created_at >= DATE_TRUNC('month', NOW())", nativeQuery = true)
    long countNewCandidatesThisMonth();

    @Query(value = "SELECT COUNT(*) FROM assessments", nativeQuery = true)
    long countTotalAssessments();

    @Query(value = "SELECT COUNT(*) FROM assessments WHERE status = 'IN_PROGRESS' AND is_active = true", nativeQuery = true)
    long countInProgressAssessments();

    @Query(value = "SELECT COUNT(*) FROM assessments WHERE status = 'PENDING' AND is_active = true", nativeQuery = true)
    long countPendingAssessments();

    @Query(value = "SELECT COUNT(*) FROM assessments WHERE status = 'COMPLETED' AND is_active = true", nativeQuery = true)
    long countCompletedAssessments();

    @Query(value = "SELECT COUNT(*) FROM questions WHERE is_active = true", nativeQuery = true)
    long countTotalQuestions();

    @Query(value = "SELECT COUNT(*) FROM categories WHERE is_active = true", nativeQuery = true)
    long countTotalCategories();

    @Query(value = "SELECT AVG(ai_score) FROM submissions WHERE ai_score IS NOT NULL", nativeQuery = true)
    Double avgAiScore();

    @Query(value = """
            SELECT AVG(ai_score) FROM submissions
            WHERE ai_score IS NOT NULL
              AND evaluated_at >= DATE_TRUNC('month', NOW() - INTERVAL '1 month')
              AND evaluated_at <  DATE_TRUNC('month', NOW())
            """, nativeQuery = true)
    Double avgAiScoreLastMonth();

    // Assessment Status Breakdown

    @Query(value = """
            SELECT status, COUNT(*) AS count
            FROM assessments
            WHERE created_at >= NOW() - INTERVAL '1 day' * :days
              AND is_active = true
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> findAssessmentStatusBreakdown(@Param("days") int days);

    // Candidate Pipeline

    @Query(value = "SELECT COUNT(*) FROM candidates WHERE is_active = true", nativeQuery = true)
    long countTotalApplied();

    @Query(value = "SELECT COUNT(DISTINCT candidate_id) FROM assessments", nativeQuery = true)
    long countTotalAssessed();

    @Query(value = """
            SELECT COUNT(DISTINCT s.candidate_id)
            FROM submissions s
            WHERE s.ai_score IS NOT NULL
            """, nativeQuery = true)
    long countTotalEvaluated();

    @Query(value = """
            SELECT designation, COUNT(*) AS count
            FROM candidates
            WHERE is_active = true
            GROUP BY designation
            """, nativeQuery = true)
    List<Object[]> findCandidatesByDesignation();

    // Recent Submissions

    @Query(value = """
            SELECT
              sub.id                                    AS submissionId,
              c.id                                      AS candidateId,
              CONCAT(c.first_name, ' ', c.last_name)   AS candidateName,
              c.designation                             AS designation,
              qs.language                               AS language,
              sub.ai_score                              AS aiScore,
              sub.evaluated_at                          AS evaluatedAt
            FROM submissions sub
            JOIN candidates c    ON c.id   = sub.candidate_id
            LEFT JOIN candidate_solutions cs ON cs.submission_id = sub.id
            LEFT JOIN question_solutions qs  ON qs.question_id  = cs.question_id
            WHERE sub.ai_score IS NOT NULL
            GROUP BY sub.id, c.id, c.first_name, c.last_name, c.designation, sub.ai_score, sub.evaluated_at, qs.language
            ORDER BY sub.evaluated_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findRecentSubmissions(@Param("limit") int limit);

    // Questions by Difficulty

    @Query(value = """
            SELECT difficulty, COUNT(*) AS count
            FROM questions
            WHERE is_active = true
            GROUP BY difficulty
            """, nativeQuery = true)
    List<Object[]> findQuestionsByDifficulty();

    // AI Score Distribution

    @Query(value = """
            SELECT ai_score AS score, COUNT(*) AS count
            FROM submissions
            WHERE ai_score IS NOT NULL
            GROUP BY ai_score
            ORDER BY ai_score
            """, nativeQuery = true)
    List<Object[]> findAiScoreDistribution();

    // Recent Activity

    @Query(value = """
            (SELECT 'ASSESSMENT_COMPLETED' AS type,
                    CONCAT('Assessment completed by ', c.first_name, ' ', c.last_name) AS description,
                    CONCAT(c.first_name, ' ', c.last_name) AS entityName,
                    a.completed_at AS timestamp
             FROM assessments a
             JOIN candidates c ON c.id = a.candidate_id
             WHERE a.status = 'COMPLETED' AND a.completed_at IS NOT NULL)

            UNION ALL

            (SELECT 'ASSESSMENT_STARTED',
                    CONCAT('Assessment started by ', c.first_name, ' ', c.last_name),
                    CONCAT(c.first_name, ' ', c.last_name),
                    a.started_at
             FROM assessments a
             JOIN candidates c ON c.id = a.candidate_id
             WHERE a.status = 'IN_PROGRESS' AND a.started_at IS NOT NULL)

            UNION ALL

            (SELECT 'CANDIDATE_ADDED',
                    CONCAT('New candidate ', c.first_name, ' ', c.last_name, ' added'),
                    CONCAT(c.first_name, ' ', c.last_name),
                    c.created_at
             FROM candidates c)

            UNION ALL

            (SELECT 'AI_EVALUATED',
                    CONCAT('AI evaluation complete · score ', s.ai_score),
                    CONCAT(c.first_name, ' ', c.last_name),
                    s.evaluated_at
             FROM submissions s
             JOIN candidates c ON c.id = s.candidate_id
             WHERE s.ai_score IS NOT NULL)

            UNION ALL

            (SELECT 'QUESTION_ADDED',
                    CONCAT('New question added: ', q.title),
                    q.title,
                    q.created_at
             FROM questions q
             WHERE q.is_active = true)

            UNION ALL

            (SELECT 'ASSESSMENT_SENT',
                    CONCAT('Assessment sent to ', c.first_name, ' ', c.last_name),
                    CONCAT(c.first_name, ' ', c.last_name),
                    a.created_at
             FROM assessments a
             JOIN candidates c ON c.id = a.candidate_id
             WHERE a.status = 'PENDING')

            ORDER BY timestamp DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findRecentActivity(@Param("limit") int limit);
}