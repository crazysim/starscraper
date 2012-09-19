# Users schema

# --- !Ups

ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT email_key UNIQUE (email);

# --- !Downs

ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users DROP CONSTRAINT email_key IF EXISTS;