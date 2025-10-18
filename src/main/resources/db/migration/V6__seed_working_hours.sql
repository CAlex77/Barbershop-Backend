-- V6__seed_working_hours.sql
BEGIN;

-- 0=domingo, 1=segunda ... 6=sábado
-- Barber 1
INSERT INTO working_hours (barber_id, day_of_week, start_time, end_time) VALUES
                                                                             (1, 1, '09:00', '18:00'),
                                                                             (1, 2, '09:00', '18:00'),
                                                                             (1, 3, '09:00', '18:00'),
                                                                             (1, 4, '09:00', '18:00'),
                                                                             (1, 5, '09:00', '18:00'),
                                                                             (1, 6, '09:00', '18:00')
    ON CONFLICT DO NOTHING;

-- Barber 2
INSERT INTO working_hours (barber_id, day_of_week, start_time, end_time) VALUES
                                                                             (2, 1, '09:00', '18:00'),
                                                                             (2, 2, '09:00', '18:00'),
                                                                             (2, 3, '09:00', '18:00'),
                                                                             (2, 4, '09:00', '18:00'),
                                                                             (2, 5, '09:00', '18:00'),
                                                                             (2, 6, '09:00', '18:00')
    ON CONFLICT DO NOTHING;

COMMIT;
