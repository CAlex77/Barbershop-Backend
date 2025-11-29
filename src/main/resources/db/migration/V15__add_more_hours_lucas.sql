-- V15__add_more_hours_lucas.sql
-- Adiciona mais horários de trabalho para o Barbeiro Lucas (barber_id = 1)
-- Estendendo o horário de trabalho para incluir noites e domingo

BEGIN;

-- Atualiza os horários existentes de segunda a sexta para incluir horário noturno (até 21:00)
UPDATE working_hours
SET end_time = '21:00'
WHERE barber_id = 1
  AND day_of_week IN (1, 2, 3, 4, 5);

-- Atualiza o horário de sábado para também ir até 21:00
UPDATE working_hours
SET end_time = '21:00'
WHERE barber_id = 1
  AND day_of_week = 6;

-- Adiciona horário de domingo (0=domingo) para o Barbeiro Lucas
INSERT INTO working_hours (barber_id, day_of_week, start_time, end_time)
VALUES (1, 0, '10:00', '16:00')
ON CONFLICT (barber_id, day_of_week) DO UPDATE
    SET start_time = EXCLUDED.start_time,
        end_time = EXCLUDED.end_time;

COMMIT;

