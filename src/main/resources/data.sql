-- Minimal idempotent seed data (barbers, services, clients, working hours)
BEGIN;

-- Users (barbers)
INSERT INTO users (name, email, role, phone)
VALUES
  ('Lucas Silva',   'lucas.silva@barbershop.local',   'barber', '+55 11 90000-0001'),
  ('Mariana Costa', 'mariana.costa@barbershop.local', 'barber', '+55 11 90000-0002')
ON CONFLICT (email) DO NOTHING;

-- Barbers (bind to users). Use fixed IDs 1 and 2 for predictability.
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

-- Working hours (0=domingo .. 6=sábado)
INSERT INTO working_hours (barber_id, day_of_week, start_time, end_time) VALUES
  (1, 1, '09:00', '18:00'), (1, 2, '09:00', '18:00'), (1, 3, '09:00', '18:00'),
  (1, 4, '09:00', '18:00'), (1, 5, '09:00', '18:00'), (1, 6, '09:00', '18:00'),
  (2, 1, '09:00', '18:00'), (2, 2, '09:00', '18:00'), (2, 3, '09:00', '18:00'),
  (2, 4, '09:00', '18:00'), (2, 5, '09:00', '18:00'), (2, 6, '09:00', '18:00')
ON CONFLICT DO NOTHING;

COMMIT;

