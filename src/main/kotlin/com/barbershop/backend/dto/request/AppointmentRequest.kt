package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class AppointmentRequest(
    @field:NotNull
    var barberId: Long,
    @field:NotNull
    var serviceId: Long,
    @field:NotNull
    var clientId: Long,
    @field:NotNull
    var startTime: OffsetDateTime,
    val status: String? = null
)
