-- V5__available_slots_function_tz.sql
-- Função de slots disponíveis considerando fuso horário local.

BEGIN;

CREATE OR REPLACE FUNCTION available_slots(
    p_barber_id     BIGINT,
    p_day           DATE,
    p_service_id    BIGINT,
    p_step_minutes  INT   DEFAULT 15,
    p_tz            TEXT  DEFAULT 'America/Sao_Paulo'
)
    RETURNS TABLE (
                      slot_start timestamptz,
                      slot_end   timestamptz
                  )
    LANGUAGE sql
AS $func$
WITH svc AS (
    SELECT s.duration_minutes
    FROM services s
    WHERE s.service_id = p_service_id
),
     wh AS (
         -- monta janelas do dia no fuso local (p_tz) e converte para timestamptz
         SELECT
             make_timestamptz(EXTRACT(YEAR  FROM p_day)::int,
                              EXTRACT(MONTH FROM p_day)::int,
                              EXTRACT(DAY   FROM p_day)::int,
                              EXTRACT(HOUR   FROM start_time)::int,
                              EXTRACT(MINUTE FROM start_time)::int,
                              0, p_tz) AS start_ts,
             make_timestamptz(EXTRACT(YEAR  FROM p_day)::int,
                              EXTRACT(MONTH FROM p_day)::int,
                              EXTRACT(DAY   FROM p_day)::int,
                              EXTRACT(HOUR   FROM end_time)::int,
                              EXTRACT(MINUTE FROM end_time)::int,
                              0, p_tz) AS end_ts
         FROM working_hours
         WHERE barber_id = p_barber_id
           -- dia da semana computado no fuso local
           AND day_of_week = EXTRACT(DOW FROM (p_day::timestamp AT TIME ZONE p_tz))
     ),
     slots AS (
         -- gera candidatos respeitando a duração do serviço
         SELECT
             gs AS slot_start,
             gs + make_interval(mins => (SELECT duration_minutes FROM svc)) AS slot_end
         FROM wh
                  CROSS JOIN LATERAL generate_series(
                 wh.start_ts,
                 wh.end_ts - make_interval(mins => (SELECT duration_minutes FROM svc)),
                 make_interval(mins => p_step_minutes)
                                     ) AS gs
     ),
     filtered AS (
         SELECT s.*
         FROM slots s
         WHERE s.slot_start >= now()
           AND NOT EXISTS (
             SELECT 1
             FROM blocked_slots b
             WHERE b.barber_id = p_barber_id
               AND tstzrange(b.start_time, b.end_time, '[)') &&
                   tstzrange(s.slot_start, s.slot_end, '[)')
         )
           AND NOT EXISTS (
             SELECT 1
             FROM appointments a
             WHERE a.barber_id = p_barber_id
               AND tstzrange(a.start_time, a.end_time, '[)') &&
                   tstzrange(s.slot_start, s.slot_end, '[)')
         )
     )
SELECT slot_start, slot_end
FROM filtered
ORDER BY slot_start;
$func$;

COMMIT;
