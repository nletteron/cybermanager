CREATE SCHEMA IF NOT EXISTS cybermanager;

CREATE TABLE IF NOT EXISTS cybermanager.cm_users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT pk_cm_users PRIMARY KEY (id),
    CONSTRAINT uk_cm_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS cybermanager.cm_user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(255),
    CONSTRAINT fk_cm_user_roles_user
        FOREIGN KEY (user_id) REFERENCES cybermanager.cm_users (id)
);
