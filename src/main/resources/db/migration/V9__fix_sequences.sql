-- V9__fix_sequences.sql
-- Ajusta as sequences para evitar conflito de chave primária após seeds com IDs explícitos.

DO $$
BEGIN
  -- users
  PERFORM setval(pg_get_serial_sequence('users','user_id'), COALESCE((SELECT MAX(user_id) FROM users), 0));
  -- clients
  PERFORM setval(pg_get_serial_sequence('clients','client_id'), COALESCE((SELECT MAX(client_id) FROM clients), 0));
  -- barbers
  PERFORM setval(pg_get_serial_sequence('barbers','barber_id'), COALESCE((SELECT MAX(barber_id) FROM barbers), 0));
  -- services
  PERFORM setval(pg_get_serial_sequence('services','service_id'), COALESCE((SELECT MAX(service_id) FROM services), 0));
  -- appointments
  PERFORM setval(pg_get_serial_sequence('appointments','appointment_id'), COALESCE((SELECT MAX(appointment_id) FROM appointments), 0));
  -- working_hours
  PERFORM setval(pg_get_serial_sequence('working_hours','working_hour_id'), COALESCE((SELECT MAX(working_hour_id) FROM working_hours), 0));
  -- blocked_slots
  PERFORM setval(pg_get_serial_sequence('blocked_slots','blocked_slot_id'), COALESCE((SELECT MAX(blocked_slot_id) FROM blocked_slots), 0));
EXCEPTION WHEN undefined_table OR undefined_function THEN
  NULL;
END $$;

