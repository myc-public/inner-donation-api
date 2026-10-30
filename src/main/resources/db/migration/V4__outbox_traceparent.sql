SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ==========================================================
-- outbox_event.traceparent (EDA3, DE20) : le routeur Debezium ne place que des colonnes en en-tete Kafka,
-- il ne sait pas extraire une cle du JSON de headers.
-- ==========================================================

ALTER TABLE outbox_event
    ADD COLUMN traceparent VARCHAR(55) NULL;
