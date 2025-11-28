package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * Request to reschedule an existing appointment.
 * startTime must be in UTC (e.g., 2025-11-27T14:30:00Z)
 */
data class RescheduleAppointmentRequest(
    @field:NotNull
    val barberId: Long,
    @field:NotNull
    val serviceId: Long,
    @field:NotNull
    @field:Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")
    val startTime: String,
    val tz: String? = "America/Sao_Paulo"
)

