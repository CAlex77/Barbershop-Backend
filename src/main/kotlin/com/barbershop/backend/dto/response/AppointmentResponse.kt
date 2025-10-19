package com.barbershop.backend.dto.response

import java.math.BigDecimal
import java.time.OffsetDateTime

data class AppointmentResponse(
    val appointmentId: Long?,
    val barberId: Long,
    val serviceId: Long,
    val clientId: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime?,
    val status: String,
    val totalPrice: BigDecimal?
)

