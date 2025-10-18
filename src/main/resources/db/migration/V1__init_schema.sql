-- V1__init_schema.sql
-- Schema base com identidade, perfis e agendamentos.

BEGIN;

-- 1) Núcleo de identidade
CREATE TABLE IF NOT EXISTS users (
                                     user_id       BIGSERIAL PRIMARY KEY,
                                     name          VARCHAR(255) NOT NULL,
                                     email         VARCHAR(255) UNIQUE,
                                     password_hash TEXT,
                                     phone         VARCHAR(40),
                                     role          TEXT NOT NULL DEFAULT 'client',        -- client | barber | admin...
                                     created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2) Perfil de cliente (1–1 com users)
CREATE TABLE IF NOT EXISTS clients (
                                       client_id BIGSERIAL PRIMARY KEY,
                                       user_id   BIGINT NOT NULL UNIQUE
                                           REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3) Perfil de barbeiro (N barbeiros, cada um vinculado a um user)
CREATE TABLE IF NOT EXISTS barbers (
                                       barber_id BIGSERIAL PRIMARY KEY,
                                       user_id   BIGINT NOT NULL
                                           REFERENCES users(user_id) ON DELETE CASCADE,
                                       name      VARCHAR(120) NOT NULL,
                                       phone     VARCHAR(40)
);
CREATE INDEX IF NOT EXISTS idx_barbers_user_id ON barbers(user_id);

-- 4) Serviços ofertados
CREATE TABLE IF NOT EXISTS services (
                                        service_id       BIGSERIAL PRIMARY KEY,
                                        name             VARCHAR(120) NOT NULL,
                                        price            NUMERIC(12,2) NOT NULL DEFAULT 0,
                                        duration_minutes INT NOT NULL DEFAULT 30 CHECK (duration_minutes > 0),
                                        is_active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5) Agendamentos
CREATE TABLE IF NOT EXISTS appointments (
                                            appointment_id BIGSERIAL PRIMARY KEY,
                                            barber_id      BIGINT  NOT NULL
                                                REFERENCES barbers(barber_id) ON DELETE RESTRICT,
                                            service_id     BIGINT  NOT NULL
                                                REFERENCES services(service_id) ON DELETE RESTRICT,
                                            client_id      BIGINT  NOT NULL
                                                REFERENCES clients(client_id) ON DELETE RESTRICT,
                                            start_time     TIMESTAMPTZ NOT NULL,
                                            status         TEXT NOT NULL DEFAULT 'SCHEDULED',    -- booked/confirmed/done/canceled...
                                            total_price    NUMERIC(12,2),
                                            created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices úteis
CREATE INDEX IF NOT EXISTS idx_appts_barber_time ON appointments(barber_id, start_time);
CREATE INDEX IF NOT EXISTS idx_appts_client_time ON appointments(client_id, start_time DESC);
CREATE INDEX IF NOT EXISTS idx_services_active   ON services(is_active);

COMMIT;
