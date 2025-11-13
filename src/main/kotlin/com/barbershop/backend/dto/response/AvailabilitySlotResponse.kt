package com.barbershop.backend.dto.response

import java.time.OffsetDateTime

data class AvailabilitySlotResponse(
    val start: OffsetDateTime,
    val end: OffsetDateTime
)

