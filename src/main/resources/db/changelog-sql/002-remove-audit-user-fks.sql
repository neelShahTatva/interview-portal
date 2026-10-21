--liquibase formatted sql

--changeset interview-portal:015-remove-audit-user-fks
-- IF EXISTS makes this safe on a brand-new database, where these audit FKs were never created.
ALTER TABLE "assessment_questions" DROP CONSTRAINT IF EXISTS "assessment_questions_created_by_fkey";
ALTER TABLE "candidates"           DROP CONSTRAINT IF EXISTS "candidates_created_by_fkey";
ALTER TABLE "candidates"           DROP CONSTRAINT IF EXISTS "candidates_updated_by_fkey";
ALTER TABLE "assessments"          DROP CONSTRAINT IF EXISTS "assessments_created_by_fkey";
ALTER TABLE "assessments"          DROP CONSTRAINT IF EXISTS "assessments_updated_by_fkey";
ALTER TABLE "questions"            DROP CONSTRAINT IF EXISTS "questions_created_by_fkey";
ALTER TABLE "questions"            DROP CONSTRAINT IF EXISTS "questions_updated_by_fkey";
ALTER TABLE "submissions"          DROP CONSTRAINT IF EXISTS "submissions_created_by_fkey";
ALTER TABLE "submissions"          DROP CONSTRAINT IF EXISTS "submissions_updated_by_fkey";
ALTER TABLE "users"                DROP CONSTRAINT IF EXISTS "users_created_by_fkey";
ALTER TABLE "users"                DROP CONSTRAINT IF EXISTS "users_updated_by_fkey";
