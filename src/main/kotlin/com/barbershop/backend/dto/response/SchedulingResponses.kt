package com.barbershop.backend.dto.response

import java.math.BigDecimal
import java.time.OffsetDateTime

// Represents a single available slot for scheduling
data class AvailabilitySlotResponse(
    val slotStart: OffsetDateTime,
    val slotEnd: OffsetDateTime
)

// Represents an appointment payload for API responses
data class AppointmentResponse(
    val id: Long,
    val clientId: Long,
    val barberId: Long,
    val serviceId: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime?,
    val status: String,
    val totalPrice: BigDecimal?
)

