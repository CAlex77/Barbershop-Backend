package com.barbershop.backend.repository

import com.barbershop.backend.dto.response.BookedAppointmentResponse
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.ZoneOffset
import java.time.Instant
import java.sql.Timestamp // added

@Repository
class AppointmentNativeRepository(
    private val jdbc: NamedParameterJdbcTemplate
) {
    fun bookAppointment(
        clientId: Long,
        barberId: Long,
        serviceId: Long,
        startUtc: Instant,
        tz: String
    ): BookedAppointmentResponse {
        val sql = """
            SELECT appointment_id,
                   slot_start,
                   slot_end,
                   status,
                   total_price
              FROM book_appointment(:clientId, :barberId, :serviceId, :startTime, :tz)
        """.trimIndent()

        // Convert Instant to java.sql.Timestamp to ensure PostgreSQL infers timestamptz correctly
        val params = mapOf(
            "clientId" to clientId,
            "barberId" to barberId,
            "serviceId" to serviceId,
            "startTime" to Timestamp.from(startUtc), // changed from Instant to Timestamp
            "tz" to tz
        )

        return jdbc.query(sql, params) { rs, _ ->
            BookedAppointmentResponse(
                appointmentId = rs.getLong("appointment_id"),
                start = rs.getTimestamp("slot_start").toInstant().atOffset(ZoneOffset.UTC),
                end = rs.getTimestamp("slot_end").toInstant().atOffset(ZoneOffset.UTC),
                status = rs.getString("status"),
                totalPrice = rs.getBigDecimal("total_price") ?: BigDecimal.ZERO
            )
        }.firstOrNull() ?: throw IllegalStateException("Não foi possível reservar o horário.")
    }

    /**
     * Validates if a slot is available for rescheduling (excluding a specific appointment).
     * Throws exception if slot is invalid or unavailable.
     */
    fun validateSlotForReschedule(
        appointmentId: Long,
        barberId: Long,
        serviceId: Long,
        startUtc: Instant,
        tz: String
    ): SlotValidationResult {
        val sql = """
            WITH svc AS (
                SELECT duration_minutes, price
                FROM services
                WHERE service_id = :serviceId
            ),
            slot_info AS (
                SELECT 
                    :startTime AS start_time,
                    :startTime + make_interval(mins => (SELECT duration_minutes FROM svc)) AS end_time,
                    (SELECT duration_minutes FROM svc) AS duration,
                    (SELECT price FROM svc) AS price
            )
            SELECT 
                si.end_time,
                si.duration,
                si.price,
                -- Check if in the past
                CASE WHEN si.start_time < now() THEN 'past' ELSE NULL END AS time_error,
                -- Check working hours
                CASE WHEN NOT EXISTS (
                    SELECT 1 FROM working_hours wh
                    WHERE wh.barber_id = :barberId
                      AND wh.day_of_week = EXTRACT(DOW FROM (si.start_time AT TIME ZONE :tz)::date)
                      AND wh.start_time <= (si.start_time AT TIME ZONE :tz)::time
                      AND wh.end_time >= (si.end_time AT TIME ZONE :tz)::time
                ) THEN 'working_hours' ELSE NULL END AS working_error,
                -- Check blocked slots
                CASE WHEN EXISTS (
                    SELECT 1 FROM blocked_slots b
                    WHERE b.barber_id = :barberId
                      AND tstzrange(b.start_time, b.end_time, '[)') && 
                          tstzrange(si.start_time, si.end_time, '[)')
                ) THEN 'blocked' ELSE NULL END AS blocked_error,
                -- Check appointment conflicts (excluding current appointment)
                CASE WHEN EXISTS (
                    SELECT 1 FROM appointments a
                    WHERE a.barber_id = :barberId
                      AND a.appointment_id != :appointmentId
                      AND tstzrange(a.start_time, a.end_time, '[)') && 
                          tstzrange(si.start_time, si.end_time, '[)')
                ) THEN 'conflict' ELSE NULL END AS conflict_error
            FROM slot_info si
        """.trimIndent()

        val params = mapOf(
            "appointmentId" to appointmentId,
            "barberId" to barberId,
            "serviceId" to serviceId,
            "startTime" to Timestamp.from(startUtc),
            "tz" to tz
        )

        return jdbc.query(sql, params) { rs, _ ->
            val timeError = rs.getString("time_error")
            val workingError = rs.getString("working_error")
            val blockedError = rs.getString("blocked_error")
            val conflictError = rs.getString("conflict_error")

            when {
                timeError != null -> throw IllegalArgumentException("Horário no passado")
                workingError != null -> throw IllegalArgumentException("Fora do horário de trabalho do barbeiro")
                blockedError != null -> throw IllegalArgumentException("Horário bloqueado")
                conflictError != null -> throw IllegalArgumentException("Horário já ocupado")
                else -> SlotValidationResult(
                    endTime = rs.getTimestamp("end_time").toInstant().atOffset(ZoneOffset.UTC),
                    durationMinutes = rs.getInt("duration"),
                    totalPrice = rs.getBigDecimal("price") ?: BigDecimal.ZERO
                )
            }
        }.firstOrNull() ?: throw IllegalArgumentException("Serviço não encontrado")
    }
}

data class SlotValidationResult(
    val endTime: java.time.OffsetDateTime,
    val durationMinutes: Int,
    val totalPrice: BigDecimal
)
