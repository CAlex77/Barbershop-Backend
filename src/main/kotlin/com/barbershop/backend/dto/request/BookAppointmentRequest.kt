package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * startTime deve estar em UTC (ex.: 2025-11-13T12:30:00Z)
 */
data class BookAppointmentRequest(
    @field:NotNull
    val clientId: Long,
    @field:NotNull
    val barberId: Long,
    @field:NotNull
    val serviceId: Long,
    @field:NotNull
    @field:Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")
    val startTime: String,
    val tz: String? = "America/Sao_Paulo"
)

