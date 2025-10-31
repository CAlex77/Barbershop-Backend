-- V10: Add soft delete flags, appointment version, and service category
BEGIN;

-- Soft delete flags
ALTER TABLE IF EXISTS clients ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS idx_clients_active ON clients(is_active);

ALTER TABLE IF EXISTS barbers ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS idx_barbers_active ON barbers(is_active);

-- Service category for filtering
ALTER TABLE IF EXISTS services ADD COLUMN IF NOT EXISTS category VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_services_category ON services(category);

-- Optimistic locking column
ALTER TABLE IF EXISTS appointments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

COMMIT;
