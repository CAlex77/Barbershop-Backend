package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull

/**
 * startTime pode estar em UTC (ex.: 2025-11-13T12:30:00Z) ou em formato local sem sufixo de fuso
 */
data class BookAppointmentRequest(
    @field:NotNull
    var clientId: Long,
    @field:NotNull
    var barberId: Long,
    @field:NotNull
    var serviceId: Long,
    @field:NotNull
    var startTime: String,
    // Optional timezone identifier (IANA), e.g. "America/Manaus". If omitted, backend will
    // prefer to parse an absolute instant when provided, otherwise fall back to UTC.
    val tz: String? = null
)
