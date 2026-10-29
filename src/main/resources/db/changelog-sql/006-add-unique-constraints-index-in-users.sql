--liquibase formatted sql

--changeset interview-portal:019-add-unique-username-active-index
CREATE UNIQUE INDEX uk_users_username_active
ON "users" USING btree ("username")
WHERE ("is_deleted" = false);

--changeset interview-portal:020-add-unique-email-active-index
CREATE UNIQUE INDEX uk_users_email_active
ON "users" USING btree ("email")
WHERE ("is_deleted" = false);