-- V4__end_time_range_and_no_overlap.sql
-- Adiciona end_time, coluna gerada time_range e constraint de não-sobreposição.

BEGIN;

-- 1) Adicionar coluna end_time
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS end_time TIMESTAMPTZ;

-- 2) Trigger para calcular end_time a partir do service.duration_minutes
CREATE OR REPLACE FUNCTION set_appointment_end_time()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_minutes INT;
BEGIN
    SELECT s.duration_minutes
    INTO v_minutes
    FROM services s
    WHERE s.service_id = NEW.service_id;

    IF v_minutes IS NULL THEN
        RAISE EXCEPTION 'Service % not found or has null duration', NEW.service_id;
    END IF;

    NEW.end_time := NEW.start_time + make_interval(mins => v_minutes);
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_set_end_time ON appointments;

CREATE TRIGGER trg_set_end_time
    BEFORE INSERT OR UPDATE OF start_time, service_id
    ON appointments
    FOR EACH ROW
EXECUTE FUNCTION set_appointment_end_time();

-- 3) Backfill do end_time para registros existentes
UPDATE appointments a
SET end_time = a.start_time + make_interval(mins => s.duration_minutes)
FROM services s
WHERE s.service_id = a.service_id
  AND a.end_time IS NULL;

-- 4) Coluna gerada: intervalo do agendamento [start, end)
--    (A expressão é IMUTÁVEL dado start_time e end_time)
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS time_range tstzrange
        GENERATED ALWAYS AS (tstzrange(start_time, end_time, '[)')) STORED;

-- 5) Extensão e constraint de exclusão (sem funções não-imutáveis no índice)
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Se já existir, não recria
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'no_overlap_for_barber'
              AND conrelid = 'appointments'::regclass
        ) THEN
            ALTER TABLE appointments
                ADD CONSTRAINT no_overlap_for_barber
                    EXCLUDE USING gist (
                    barber_id WITH =,
                    time_range WITH &&
                    );
        END IF;
    END
$$;

COMMIT;
