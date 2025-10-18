-- Initial schema for Barbershop demo
-- Create basic tables for barbers, services and appointments

CREATE TABLE IF NOT EXISTS barbers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS services (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    price NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS appointments (
    id SERIAL PRIMARY KEY,
    barber_id INTEGER NOT NULL REFERENCES barbers(id) ON DELETE CASCADE,
    service_id INTEGER NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    customer_name VARCHAR(120) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'SCHEDULED'
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_appointments_barber_id ON appointments(barber_id);
CREATE INDEX IF NOT EXISTS idx_appointments_start_time ON appointments(start_time);

