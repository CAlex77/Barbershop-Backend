package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull

/**
 * Request to reschedule an existing appointment.
 * startTime can be UTC (e.g., 2025-11-27T14:30:00Z) or local without zone (e.g., 2025-11-27T09:55:00).
 */
data class RescheduleAppointmentRequest(
    @field:NotNull
    val barberId: Long,
    @field:NotNull
    val serviceId: Long,
    @field:NotNull
    val startTime: String,
    // Optional timezone identifier (IANA), e.g. "America/Manaus". If omitted, backend will
    // prefer to parse an absolute instant when provided, otherwise fall back to UTC (or Manaus default behavior in AppointmentService).
    val tz: String? = null
)
