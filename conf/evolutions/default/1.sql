# Users schema

# --- !Ups

CREATE TABLE user (
    "id" SERIAL;
    "email" varchar(255) NOT NULL;
);

# --- !Downs

DROP TABLE user;