-- Seed demo data for Barbershop

-- Barbers
INSERT INTO barbers (id, name, phone) VALUES
    (1, 'Lucas Silva', '+55 11 90000-0001')
ON CONFLICT DO NOTHING;

INSERT INTO barbers (id, name, phone) VALUES
    (2, 'Mariana Costa', '+55 11 90000-0002')
ON CONFLICT DO NOTHING;

-- Services
INSERT INTO services (id, name, price) VALUES
    (1, 'Corte Masculino', 49.90)
ON CONFLICT DO NOTHING;

INSERT INTO services (id, name, price) VALUES
    (2, 'Barba Completa', 39.90)
ON CONFLICT DO NOTHING;

INSERT INTO services (id, name, price) VALUES
    (3, 'Corte + Barba', 79.90)
ON CONFLICT DO NOTHING;

-- Appointments
INSERT INTO appointments (id, barber_id, service_id, customer_name, start_time, status) VALUES
    (1, 1, 1, 'João Pedro', NOW() + INTERVAL '1 day', 'SCHEDULED')
ON CONFLICT DO NOTHING;

INSERT INTO appointments (id, barber_id, service_id, customer_name, start_time, status) VALUES
    (2, 2, 2, 'Ana Paula', NOW() + INTERVAL '2 days', 'SCHEDULED')
ON CONFLICT DO NOTHING;

