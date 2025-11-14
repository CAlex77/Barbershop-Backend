-- V13__fix_book_appointment_ambiguity.sql
-- Qualify columns in RETURNING to avoid ambiguity with OUT parameters having the same names.

CREATE OR REPLACE FUNCTION book_appointment(
    p_client_id   BIGINT,
    p_barber_id   BIGINT,
    p_service_id  BIGINT,
    p_start_time  timestamptz,             -- instante em UTC
    p_tz          TEXT DEFAULT 'America/Sao_Paulo'
)
    RETURNS TABLE (
                      appointment_id BIGINT,
                      slot_start     timestamptz,
                      slot_end       timestamptz,
                      status         TEXT,
                      total_price    numeric
                  )
    LANGUAGE plpgsql
AS $func$
DECLARE
    v_duration     INT;
    v_price        numeric;
    v_end_time     timestamptz;
    v_local_date   date;
    v_start_local  time;
    v_end_local    time;
BEGIN
    IF p_start_time < now() THEN
        RAISE EXCEPTION 'Start time (%) no passado', p_start_time
            USING ERRCODE = '22023';
    END IF;

    SELECT s.duration_minutes, s.price
    INTO v_duration, v_price
    FROM services s
    WHERE s.service_id = p_service_id;

    IF v_duration IS NULL THEN
        RAISE EXCEPTION 'Serviço % inexistente', p_service_id
            USING ERRCODE = '22023';
    END IF;

    v_end_time := p_start_time + make_interval(mins => v_duration);

    -- trava por barbeiro (evita corrida)
    PERFORM pg_advisory_xact_lock(p_barber_id::bigint);

    -- converte para horário local
    v_local_date  := (p_start_time AT TIME ZONE p_tz)::date;
    v_start_local := (p_start_time AT TIME ZONE p_tz)::time;
    v_end_local   := (v_end_time   AT TIME ZONE p_tz)::time;

    -- valida janela dentro do expediente do barbeiro, no fuso local
    IF NOT EXISTS (
        SELECT 1
        FROM working_hours wh
        WHERE wh.barber_id = p_barber_id
          AND wh.day_of_week = EXTRACT(DOW FROM (v_local_date::timestamp AT TIME ZONE p_tz))
          AND wh.start_time <= v_start_local
          AND wh.end_time   >= v_end_local
    )
    THEN
        RAISE EXCEPTION 'Fora do horário de trabalho do barbeiro % em % (fuso %)', p_barber_id, p_start_time, p_tz
            USING ERRCODE = '22023';
    END IF;

    -- conflitos
    IF EXISTS (
        SELECT 1
        FROM blocked_slots b
        WHERE b.barber_id = p_barber_id
          AND tstzrange(b.start_time, b.end_time, '[)') &&
              tstzrange(p_start_time, v_end_time, '[)')
    )
    THEN
        RAISE EXCEPTION 'Conflito com bloqueio de agenda do barbeiro %', p_barber_id
            USING ERRCODE = '22023';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM appointments a
        WHERE a.barber_id = p_barber_id
          AND tstzrange(a.start_time, a.end_time, '[)') &&
              tstzrange(p_start_time, v_end_time, '[)')
    )
    THEN
        RAISE EXCEPTION 'Horário já ocupado para o barbeiro %', p_barber_id
            USING ERRCODE = '22023';
    END IF;

    -- cria e retorna, qualificando colunas para evitar ambiguidade com OUT params
    INSERT INTO appointments (barber_id, service_id, client_id, start_time, status, total_price)
    VALUES (p_barber_id, p_service_id, p_client_id, p_start_time, 'SCHEDULED', v_price)
    RETURNING appointments.appointment_id,
              appointments.start_time,
              appointments.end_time,
              appointments.status,
              appointments.total_price
        INTO appointment_id, slot_start, slot_end, status, total_price;

    RETURN NEXT;
END;
$func$;
