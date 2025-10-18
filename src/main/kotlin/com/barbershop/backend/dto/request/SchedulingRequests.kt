package com.barbershop.backend.dto.request

import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

// Availability for GET uses query params; removed unused AvailabilityRequest data class.

data class AppointmentCreateRequest(
    @field:Positive val clientId: Long,
    @field:Positive val barberId: Long,
    @field:Positive val serviceId: Long,
    val startTime: OffsetDateTime,
    val tz: String? = null
)
