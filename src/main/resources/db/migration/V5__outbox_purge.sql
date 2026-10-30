SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ==========================================================
-- Purge de outbox_event (EDA3) : declaree ici, executee par la base, aucun code dans l'API.
-- Aucun evenement perdu : Debezium lit le binlog (30 jours), pas la table, et ignore ces suppressions
-- (skipped.operations=u,d). Changer la retention = nouvelle migration.
-- ==========================================================

CREATE EVENT evt_outbox_purge
    ON SCHEDULE EVERY 1 DAY
    COMMENT 'Purge de outbox_event : retention 7 jours (EDA3)'
    DO DELETE FROM outbox_event WHERE occurred_at < NOW(6) - INTERVAL 7 DAY;
