# Users schema

# --- !Ups

CREATE TABLE users (
    "id" SERIAL NOT NULL,
    "email" varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE users;