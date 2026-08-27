CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    registration_date DATETIME(6) NOT NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    lock_time DATETIME(6) NULL,
    rol VARCHAR(255) NULL,
    user_status VARCHAR(30) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_dni UNIQUE (dni)
);

CREATE TABLE account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(255) NULL,
    alias VARCHAR(255) NULL,
    balance DECIMAL(38,2) NULL,
    cbu VARCHAR(255) NULL,
    creation_date DATETIME(6) NULL,
    mask_alias VARCHAR(255) NULL,
    type_account VARCHAR(255) NULL,
    status_account VARCHAR(255) NULL,
    currency VARCHAR(255) NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(19,2) NULL,
    creation_date DATETIME(6) NULL,
    description VARCHAR(255) NULL,
    reference VARCHAR(255) NULL,

    movement_account_type VARCHAR(30) NULL,
    status_transaction VARCHAR(30) NULL,
    type_transaction VARCHAR(30) NULL,
    currency VARCHAR(10) NULL,

    account_origin_id BIGINT NULL,
    account_destiny_id BIGINT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_transaction_reference UNIQUE (reference),

    CONSTRAINT fk_transaction_account_origin
        FOREIGN KEY (account_origin_id)
        REFERENCES account (id),

    CONSTRAINT fk_transaction_account_destiny
        FOREIGN KEY (account_destiny_id)
        REFERENCES account (id)
);

CREATE TABLE loan (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(19,2) NOT NULL,
    installments INT NOT NULL,
    interest_rate DECIMAL(10,4) NOT NULL,
    total_to_pay DECIMAL(19,2) NOT NULL,
    installments_amount DECIMAL(19,2) NOT NULL,
    start_date DATETIME(6) NULL,
    end_date DATETIME(6) NULL,
    status_loan VARCHAR(30) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    account_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_loan_account
        FOREIGN KEY (account_id)
        REFERENCES account (id)
);

CREATE TABLE payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NULL,
    amount DECIMAL(19,2) NOT NULL,
    payment_date DATETIME(6) NOT NULL,
    service_entity VARCHAR(40) NOT NULL,
    status_payment VARCHAR(30) NOT NULL,
    account_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_payment_account
        FOREIGN KEY (account_id)
        REFERENCES account (id)
);

CREATE TABLE card (
    id BIGINT NOT NULL AUTO_INCREMENT,
    card_number VARCHAR(19) NOT NULL,
    expiration_date DATETIME(6) NOT NULL,
    cvv VARCHAR(4) NOT NULL,
    type_card VARCHAR(30) NOT NULL,
    brand VARCHAR(30) NOT NULL,
    status_card VARCHAR(30) NOT NULL,
    account_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_card_number UNIQUE (card_number),

    CONSTRAINT fk_card_account
        FOREIGN KEY (account_id)
        REFERENCES account (id)
);

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type_notification VARCHAR(50) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6) NULL,
    reference_type VARCHAR(40) NULL,
    reference_id BIGINT NULL,
    recipient_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id)
        REFERENCES users (id)
);

CREATE INDEX idx_notification_recipient_read
    ON notification (recipient_id, is_read);

CREATE INDEX idx_notification_created_at
    ON notification (created_at);


CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    date_time DATETIME(6) NOT NULL,
    ip_origin VARCHAR(45) NULL,
    location VARCHAR(150) NULL,
    type VARCHAR(40) NOT NULL,
    user_id BIGINT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_audit_log_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE ip_address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ip VARCHAR(45) NOT NULL,
    registration_date DATETIME(6) NOT NULL,
    suspicious BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_ip_address_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(512) NOT NULL,
    revoked BOOLEAN NOT NULL,
    expired BOOLEAN NOT NULL,
    jwt_token_type VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_token UNIQUE (token),

    CONSTRAINT fk_token_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_token_user_id
    ON token (user_id);


CREATE TABLE idempotency_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    idempotency_key VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    operation VARCHAR(50) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    status VARCHAR(30) NOT NULL,
    response_body TEXT NULL,
    response_status_code INT NULL,
    resource_id BIGINT NULL,
    error_message VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    expires_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_idempotency_record
        UNIQUE (idempotency_key, user_id, operation),

    CONSTRAINT fk_idempotency_record_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE security_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(255) NULL,
    expired BOOLEAN NOT NULL,
    revoked BOOLEAN NOT NULL,
    created_at DATETIME(6) NULL,
    expiration DATETIME(6) NULL,

    PRIMARY KEY (id)
);