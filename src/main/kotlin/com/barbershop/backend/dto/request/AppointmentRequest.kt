package com.barbershop.backend.dto.request

import java.time.OffsetDateTime

data class AppointmentRequest(
    val barberId: Long,
    val serviceId: Long,
    val clientId: Long,
    val startTime: OffsetDateTime,
    val status: String? = null
)

