-- V3__working_hours_and_blocked_slots.sql
-- Define horários semanais e bloqueios de agenda por barbeiro.

BEGIN;

-- 1) Horários fixos semanais de cada barbeiro
CREATE TABLE IF NOT EXISTS working_hours (
                                             working_hour_id BIGSERIAL PRIMARY KEY,
                                             barber_id       BIGINT NOT NULL
                                             REFERENCES barbers(barber_id) ON DELETE CASCADE,
    day_of_week     INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  -- 0=domingo
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    CHECK (start_time < end_time),
    UNIQUE (barber_id, day_of_week, start_time, end_time)
    );

CREATE INDEX IF NOT EXISTS idx_working_hours_barber_day
    ON working_hours(barber_id, day_of_week);

-- 2) Bloqueios específicos (folgas, férias, manutenção, etc)
CREATE TABLE IF NOT EXISTS blocked_slots (
                                             blocked_slot_id BIGSERIAL PRIMARY KEY,
                                             barber_id       BIGINT NOT NULL
                                             REFERENCES barbers(barber_id) ON DELETE CASCADE,
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    reason          VARCHAR(255),
    CHECK (start_time < end_time)
    );

CREATE INDEX IF NOT EXISTS idx_blocked_slots_barber_time
    ON blocked_slots(barber_id, start_time, end_time);

COMMIT;
