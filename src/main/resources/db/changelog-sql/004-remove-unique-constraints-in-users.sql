--liquibase formatted sql

--changeset interview-portal:017-remove-audit-user-fks
ALTER TABLE "users" DROP CONSTRAINT IF EXISTS "users_email_key";
ALTER TABLE "users" DROP CONSTRAINT IF EXISTS "users_username_key";