-- V2__seed_demo_v2.sql
-- Seed atualizado, já no novo modelo.

BEGIN;

-- Users (barbers)
INSERT INTO users (name, email, role, phone) VALUES
                                                 ('Lucas Silva',   'lucas.silva@barbershop.local',   'barber', '+55 11 90000-0001'),
                                                 ('Mariana Costa', 'mariana.costa@barbershop.local', 'barber', '+55 11 90000-0002')
ON CONFLICT (email) DO NOTHING;

-- Barbers (amarrando ao user correspondente)
INSERT INTO barbers (barber_id, user_id, name, phone)
VALUES
    (1, (SELECT user_id FROM users WHERE email='lucas.silva@barbershop.local'),   'Lucas Silva',   '+55 11 90000-0001'),
    (2, (SELECT user_id FROM users WHERE email='mariana.costa@barbershop.local'), 'Mariana Costa', '+55 11 90000-0002')
ON CONFLICT DO NOTHING;

-- Services
INSERT INTO services (service_id, name, price, duration_minutes) VALUES
                                                                     (1, 'Corte Masculino', 49.90, 30),
                                                                     (2, 'Barba Completa',  39.90, 25),
                                                                     (3, 'Corte + Barba',   79.90, 55)
ON CONFLICT DO NOTHING;

-- Users (clients)
INSERT INTO users (name, email, role) VALUES
                                          ('João Pedro', 'joao.pedro@barbershop.local', 'client'),
                                          ('Ana Paula',  'ana.paula@barbershop.local',  'client')
ON CONFLICT (email) DO NOTHING;

-- Clients
INSERT INTO clients (user_id)
SELECT u.user_id FROM users u
WHERE u.email IN ('joao.pedro@barbershop.local','ana.paula@barbershop.local')
ON CONFLICT DO NOTHING;

-- Appointments (já com client_id)
INSERT INTO appointments (appointment_id, barber_id, service_id, client_id, start_time, status, total_price)
VALUES
    (
        1,
        1,
        1,
        (SELECT c.client_id FROM clients c JOIN users u ON u.user_id = c.user_id
         WHERE u.email='joao.pedro@barbershop.local'),
        now() + interval '1 day',
        'SCHEDULED',
        49.90
    ),
    (
        2,
        2,
        2,
        (SELECT c.client_id FROM clients c JOIN users u ON u.user_id = c.user_id
         WHERE u.email='ana.paula@barbershop.local'),
        now() + interval '2 days',
        'SCHEDULED',
        39.90
    )
ON CONFLICT DO NOTHING;

COMMIT;
