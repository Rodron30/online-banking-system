-- =========================================================
-- SAMPLE BANKING SYSTEM - PostgreSQL Schema
-- Base sa mga JPA entities: User, Account, Transaction
-- =========================================================

-- Kung gusto mong i-reset ang schema, i-uncomment ang mga sumusunod:
-- DROP TABLE IF EXISTS transactions CASCADE;
-- DROP TABLE IF EXISTS accounts CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;

-- =========================================================
-- Table: users
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    pin         VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER'
                CHECK (role IN ('USER', 'ADMIN')),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

-- =========================================================
-- Table: accounts
-- =========================================================
CREATE TABLE IF NOT EXISTS accounts (
    id              BIGSERIAL PRIMARY KEY,
    account_number  VARCHAR(10)     NOT NULL,
    user_id         BIGINT          NOT NULL,
    balance         NUMERIC(19,2)   NOT NULL DEFAULT 0.00
                    CHECK (balance >= 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'FROZEN')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP,

    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT uk_accounts_user_id        UNIQUE (user_id),
    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_accounts_account_number_format
        CHECK (account_number ~ '^\d{16}$')
);

-- =========================================================
-- Table: transactions
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions (
    id                          BIGSERIAL PRIMARY KEY,
    account_id                  BIGINT          NOT NULL,
    type                        VARCHAR(20)     NOT NULL
                                CHECK (type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT')),
    amount                      NUMERIC(19,2)   NOT NULL
                                CHECK (amount >= 0.01),
    balance_after               NUMERIC(19,2)   NOT NULL
                                CHECK (balance_after >= 0),
    counterparty_account_number VARCHAR(16),
    description                 VARCHAR(255),
    timestamp                   TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_transactions_counterparty_format
        CHECK (counterparty_account_number IS NULL
               OR counterparty_account_number ~ '^\d{10}$')
);

-- =========================================================
-- Table: notifications
-- =========================================================
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL
                CHECK (type IN (
                    'DEPOSIT', 'WITHDRAWAL', 'TRANSFER_SENT',
                    'TRANSFER_RECEIVED', 'PIN_CHANGED',
                    'ACCOUNT_FROZEN', 'ACCOUNT_ACTIVATED',
                    'ACCOUNT_DELETED'
                )),
    message     VARCHAR(255) NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
);

-- =========================================================
-- ALTER for existing databases already deployed
-- (safe to run again; IF NOT EXISTS guards each change)
-- =========================================================
ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- =========================================================
-- Indexes para sa mabilis na paghahanap
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_notifications_user_id
    ON notifications (user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
    ON notifications (user_id, is_read);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id
    ON accounts (user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id
    ON transactions (account_id);

CREATE INDEX IF NOT EXISTS idx_transactions_timestamp
    ON transactions (timestamp DESC);
