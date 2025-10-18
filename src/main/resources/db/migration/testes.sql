--tenta realizar um agendamento
SELECT * FROM book_appointment(
        p_client_id   := 1,
        p_barber_id   := 1,
        p_service_id  := 1,
        p_start_time  := date_trunc('day', now()) + interval '1 day' + interval '10 hour'
              );


--verifica horarios disponiveis para agendamento
SELECT * FROM available_slots(1, (CURRENT_DATE + 2), 1, 15, 'America/Sao_Paulo');
