--liquibase formatted sql

--changeset author:Norair id:002_create_books_table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    brand VARCHAR(100),
    stock INT NOT NULL,
    price DOUBLE PRECISION NOT NULL
);
--rollback DROP TABLE books;