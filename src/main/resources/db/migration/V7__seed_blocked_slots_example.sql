-- V7__seed_blocked_slots_example.sql
BEGIN;

INSERT INTO blocked_slots (barber_id, start_time, end_time, reason)
VALUES (
           1,
           date_trunc('day', now()) + interval '1 day' + interval '12 hour',
           date_trunc('day', now()) + interval '1 day' + interval '13 hour',
           'Almoço'
       );

COMMIT;
