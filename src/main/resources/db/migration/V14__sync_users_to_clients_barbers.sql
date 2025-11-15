-- V14__sync_users_to_clients_barbers.sql
-- Synchronize existing users into clients and barbers tables based on role.
-- Inserts clients for users with role = 'client' and inserts barbers for users with role = 'barber'.
-- Safe to run multiple times; uses NOT EXISTS guards to avoid duplicates.

BEGIN;

-- Create missing client entries for users with role 'client'
INSERT INTO clients (user_id)
SELECT u.user_id
FROM users u
WHERE lower(u.role) = 'client'
  AND NOT EXISTS (
    SELECT 1 FROM clients c WHERE c.user_id = u.user_id
  );

-- Create missing barber entries for users with role 'barber'
-- Copy name and phone from users table into barbers.name and barbers.phone
INSERT INTO barbers (user_id, name, phone)
SELECT u.user_id, u.name, u.phone
FROM users u
WHERE lower(u.role) = 'barber'
  AND NOT EXISTS (
    SELECT 1 FROM barbers b WHERE b.user_id = u.user_id
  );

COMMIT;

