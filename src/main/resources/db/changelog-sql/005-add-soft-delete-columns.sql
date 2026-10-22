--liquibase formatted sql

--changeset interview-portal:018-add-soft-delete-columns
ALTER TABLE "users"
    ADD COLUMN "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX "idx_users_is_deleted"
    ON "users" ("is_deleted");

ALTER TABLE "questions"
    ADD COLUMN "is_deleted" BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX "idx_questions_is_deleted"
    ON "questions" ("is_deleted");