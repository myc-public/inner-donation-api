SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ==========================================================
-- donor table
-- ==========================================================

CREATE TABLE IF NOT EXISTS donor (
    donor_id BINARY(16) PRIMARY KEY,
    last_name VARCHAR(120) NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL,
    date_of_birth DATE,
    country VARCHAR(80),
    created_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT uk_donor_email UNIQUE (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================================
-- donation table
-- ==========================================================

CREATE TABLE IF NOT EXISTS donation (
                                        donation_id BINARY(16) PRIMARY KEY,
    donor_id BINARY(16) NOT NULL,
    category ENUM('FOOD', 'HEALTH', 'EDUCATION', 'ENVIRONMENT', 'OTHER') NOT NULL,
    type_flag BOOLEAN NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    donation_timestamp TIMESTAMP(6) NOT NULL,

    INDEX idx_donation_donor_id (donor_id),
    INDEX idx_donation_timestamp (donation_timestamp)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================================
-- outbox_event table
-- ==========================================================

CREATE TABLE IF NOT EXISTS outbox_event (
                                            event_id BINARY(16) PRIMARY KEY,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(80) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    event_version VARCHAR(20) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    message_key VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    headers TEXT,
    occurred_at TIMESTAMP(6) NOT NULL,

    INDEX idx_outbox_occurred_at (occurred_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;