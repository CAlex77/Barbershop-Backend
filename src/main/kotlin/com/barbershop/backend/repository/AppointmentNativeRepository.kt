package com.barbershop.backend.repository

import com.barbershop.backend.dto.response.BookedAppointmentResponse
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.ZoneOffset
import java.time.Instant

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

        val params = mapOf(
            "clientId" to clientId,
            "barberId" to barberId,
            "serviceId" to serviceId,
            "startTime" to startUtc,
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
}

