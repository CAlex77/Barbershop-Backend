package com.barbershop.backend.dto.response

import java.time.OffsetDateTime

data class BlockedSlotResponse(
    val blockedSlotId: Long?,
    val barberId: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val reason: String?
)
