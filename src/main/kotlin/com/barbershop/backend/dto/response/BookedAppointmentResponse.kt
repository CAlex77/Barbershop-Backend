package com.barbershop.backend.dto.response

import java.time.OffsetDateTime
import java.math.BigDecimal

data class BookedAppointmentResponse(
    val appointmentId: Long,
    val start: OffsetDateTime,
    val end: OffsetDateTime,
    val status: String,
    val totalPrice: BigDecimal
)

