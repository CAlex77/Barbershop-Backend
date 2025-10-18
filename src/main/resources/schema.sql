-- Combined schema from former Flyway migrations (V1, V3, V4, V5, V8)
-- Idempotent DDL: uses IF NOT EXISTS where possible.

-- ========== V1: Base schema ==========
CREATE TABLE IF NOT EXISTS users (
  user_id       BIGSERIAL PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  email         VARCHAR(255) UNIQUE,
  password_hash TEXT,
  phone         VARCHAR(40),
  role          TEXT NOT NULL DEFAULT 'client',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS clients (
  client_id BIGSERIAL PRIMARY KEY,
  user_id   BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS barbers (
  barber_id BIGSERIAL PRIMARY KEY,
  user_id   BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  name      VARCHAR(120) NOT NULL,
  phone     VARCHAR(40)
);

CREATE INDEX IF NOT EXISTS idx_barbers_user_id ON barbers(user_id);

CREATE TABLE IF NOT EXISTS services (
  service_id       BIGSERIAL PRIMARY KEY,
  name             VARCHAR(120) NOT NULL,
  price            NUMERIC(12,2) NOT NULL DEFAULT 0,
  duration_minutes INT NOT NULL DEFAULT 30 CHECK (duration_minutes > 0),
  is_active        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS appointments (
  appointment_id BIGSERIAL PRIMARY KEY,
  barber_id      BIGINT NOT NULL REFERENCES barbers(barber_id) ON DELETE RESTRICT,
  service_id     BIGINT NOT NULL REFERENCES services(service_id) ON DELETE RESTRICT,
  client_id      BIGINT NOT NULL REFERENCES clients(client_id) ON DELETE RESTRICT,
  start_time     TIMESTAMPTZ NOT NULL,
  status         TEXT NOT NULL DEFAULT 'SCHEDULED',
  total_price    NUMERIC(12,2),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_appts_barber_time ON appointments(barber_id, start_time);
CREATE INDEX IF NOT EXISTS idx_appts_client_time ON appointments(client_id, start_time DESC);
CREATE INDEX IF NOT EXISTS idx_services_active   ON services(is_active);

-- ========== V3: Working hours and blocked slots ==========
CREATE TABLE IF NOT EXISTS working_hours (
  working_hour_id BIGSERIAL PRIMARY KEY,
  barber_id       BIGINT NOT NULL REFERENCES barbers(barber_id) ON DELETE CASCADE,
  day_of_week     INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  start_time      TIME NOT NULL,
  end_time        TIME NOT NULL,
  CHECK (start_time < end_time),
  UNIQUE (barber_id, day_of_week, start_time, end_time)
);

CREATE INDEX IF NOT EXISTS idx_working_hours_barber_day ON working_hours(barber_id, day_of_week);

CREATE TABLE IF NOT EXISTS blocked_slots (
  blocked_slot_id BIGSERIAL PRIMARY KEY,
  barber_id       BIGINT NOT NULL REFERENCES barbers(barber_id) ON DELETE CASCADE,
  start_time      TIMESTAMPTZ NOT NULL,
  end_time        TIMESTAMPTZ NOT NULL,
  reason          VARCHAR(255),
  CHECK (start_time < end_time)
);

CREATE INDEX IF NOT EXISTS idx_blocked_slots_barber_time ON blocked_slots(barber_id, start_time, end_time);

-- ========== V4: end_time, generated range and no-overlap constraint ==========
ALTER TABLE appointments
  ADD COLUMN IF NOT EXISTS end_time TIMESTAMPTZ;

CREATE OR REPLACE FUNCTION set_appointment_end_time()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_minutes INT; BEGIN
  SELECT s.duration_minutes INTO v_minutes FROM services s WHERE s.service_id = NEW.service_id;
  IF v_minutes IS NULL THEN RAISE EXCEPTION 'Service % not found or has null duration', NEW.service_id; END IF;
  NEW.end_time := NEW.start_time + make_interval(mins => v_minutes);
  RETURN NEW;
END; $$;

DROP TRIGGER IF EXISTS trg_set_end_time ON appointments;
CREATE TRIGGER trg_set_end_time
  BEFORE INSERT OR UPDATE OF start_time, service_id ON appointments
  FOR EACH ROW EXECUTE FUNCTION set_appointment_end_time();

UPDATE appointments a
SET end_time = a.start_time + make_interval(mins => s.duration_minutes)
FROM services s
WHERE s.service_id = a.service_id AND a.end_time IS NULL;

ALTER TABLE appointments
  ADD COLUMN IF NOT EXISTS time_range tstzrange GENERATED ALWAYS AS (tstzrange(start_time, end_time, '[)')) STORED;

CREATE EXTENSION IF NOT EXISTS btree_gist;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'no_overlap_for_barber' AND conrelid = 'appointments'::regclass
  ) THEN
    ALTER TABLE appointments
      ADD CONSTRAINT no_overlap_for_barber
      EXCLUDE USING gist (
        barber_id WITH =,
        time_range WITH &&
      );
  END IF;
END $$;

-- ========== V5: available_slots() ==========
CREATE OR REPLACE FUNCTION available_slots(
  p_barber_id     BIGINT,
  p_day           DATE,
  p_service_id    BIGINT,
  p_step_minutes  INT   DEFAULT 15,
  p_tz            TEXT  DEFAULT 'America/Sao_Paulo'
) RETURNS TABLE (slot_start timestamptz, slot_end timestamptz)
LANGUAGE sql AS $func$
WITH svc AS (
  SELECT s.duration_minutes FROM services s WHERE s.service_id = p_service_id
), wh AS (
  SELECT
    make_timestamptz(EXTRACT(YEAR FROM p_day)::int, EXTRACT(MONTH FROM p_day)::int, EXTRACT(DAY FROM p_day)::int,
                      EXTRACT(HOUR FROM start_time)::int, EXTRACT(MINUTE FROM start_time)::int, 0, p_tz) AS start_ts,
    make_timestamptz(EXTRACT(YEAR FROM p_day)::int, EXTRACT(MONTH FROM p_day)::int, EXTRACT(DAY FROM p_day)::int,
                      EXTRACT(HOUR FROM end_time)::int, EXTRACT(MINUTE FROM end_time)::int, 0, p_tz) AS end_ts
  FROM working_hours
  WHERE barber_id = p_barber_id
    AND day_of_week = EXTRACT(DOW FROM (p_day::timestamp AT TIME ZONE p_tz))
), slots AS (
  SELECT gs AS slot_start,
         gs + make_interval(mins => (SELECT duration_minutes FROM svc)) AS slot_end
  FROM wh CROSS JOIN LATERAL generate_series(
    wh.start_ts,
    wh.end_ts - make_interval(mins => (SELECT duration_minutes FROM svc)),
    make_interval(mins => p_step_minutes)
  ) AS gs
), filtered AS (
  SELECT s.* FROM slots s
  WHERE s.slot_start >= now()
    AND NOT EXISTS (
      SELECT 1 FROM blocked_slots b
      WHERE b.barber_id = p_barber_id
        AND tstzrange(b.start_time, b.end_time, '[)') && tstzrange(s.slot_start, s.slot_end, '[)')
    )
    AND NOT EXISTS (
      SELECT 1 FROM appointments a
      WHERE a.barber_id = p_barber_id
        AND tstzrange(a.start_time, a.end_time, '[)') && tstzrange(s.slot_start, s.slot_end, '[)')
    )
)
SELECT slot_start, slot_end FROM filtered ORDER BY slot_start;
$func$;

-- ========== V8: book_appointment() ==========
CREATE OR REPLACE FUNCTION book_appointment(
  p_client_id   BIGINT,
  p_barber_id   BIGINT,
  p_service_id  BIGINT,
  p_start_time  timestamptz,
  p_tz          TEXT DEFAULT 'America/Sao_Paulo'
) RETURNS TABLE (
  appointment_id BIGINT,
  slot_start     timestamptz,
  slot_end       timestamptz,
  status         TEXT,
  total_price    numeric
) LANGUAGE plpgsql AS $func$
DECLARE
  v_duration   INT;
  v_price      numeric;
  v_end_time   timestamptz;
  v_local_date date;
  v_start_local time;
  v_end_local   time;
BEGIN
  IF p_start_time < now() THEN
    RAISE EXCEPTION 'Start time (%) no passado', p_start_time USING ERRCODE = '22023';
  END IF;
  SELECT s.duration_minutes, s.price INTO v_duration, v_price FROM services s WHERE s.service_id = p_service_id;
  IF v_duration IS NULL THEN RAISE EXCEPTION 'Serviço % inexistente', p_service_id USING ERRCODE = '22023'; END IF;
  v_end_time := p_start_time + make_interval(mins => v_duration);
  PERFORM pg_advisory_xact_lock(p_barber_id::bigint);
  v_local_date  := (p_start_time AT TIME ZONE p_tz)::date;
  v_start_local := (p_start_time AT TIME ZONE p_tz)::time;
  v_end_local   := (v_end_time   AT TIME ZONE p_tz)::time;
  IF NOT EXISTS (
    SELECT 1 FROM working_hours wh
    WHERE wh.barber_id = p_barber_id
      AND wh.day_of_week = EXTRACT(DOW FROM (v_local_date::timestamp AT TIME ZONE p_tz))
      AND wh.start_time <= v_start_local
      AND wh.end_time   >= v_end_local
  ) THEN
    RAISE EXCEPTION 'Fora do horário de trabalho do barbeiro % em % (fuso %)', p_barber_id, p_start_time, p_tz USING ERRCODE = '22023';
  END IF;
  IF EXISTS (
    SELECT 1 FROM blocked_slots b
    WHERE b.barber_id = p_barber_id
      AND tstzrange(b.start_time, b.end_time, '[)') && tstzrange(p_start_time, v_end_time, '[)')
  ) THEN
    RAISE EXCEPTION 'Conflito com bloqueio de agenda do barbeiro %', p_barber_id USING ERRCODE = '22023';
  END IF;
  IF EXISTS (
    SELECT 1 FROM appointments a
    WHERE a.barber_id = p_barber_id
      AND tstzrange(a.start_time, a.end_time, '[)') && tstzrange(p_start_time, v_end_time, '[)')
  ) THEN
    RAISE EXCEPTION 'Horário já ocupado para o barbeiro %', p_barber_id USING ERRCODE = '22023';
  END IF;
  INSERT INTO appointments (barber_id, service_id, client_id, start_time, status, total_price)
  VALUES (p_barber_id, p_service_id, p_client_id, p_start_time, 'SCHEDULED', v_price)
  RETURNING appointment_id, start_time, end_time, status, total_price
    INTO appointment_id, slot_start, slot_end, status, total_price;
  RETURN NEXT;
END; $func$;

