-- V12__add_service_category.sql
-- Add optional category column to services table to match the Service entity and queries

BEGIN;

ALTER TABLE services
    ADD COLUMN IF NOT EXISTS category VARCHAR(120);

-- Optional: index to speed up case-insensitive lookups by category
-- CREATE INDEX IF NOT EXISTS idx_services_category_lower ON services (lower(category));

COMMIT;

