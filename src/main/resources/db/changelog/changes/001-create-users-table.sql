--liquibase formatted sql

--changeset author:Norair id:001_create_users_table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
--rollback DROP TABLE users;