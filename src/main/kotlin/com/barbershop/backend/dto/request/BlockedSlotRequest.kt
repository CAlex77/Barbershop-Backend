package com.barbershop.backend.dto.request

import java.time.OffsetDateTime

data class BlockedSlotRequest(
    val barberId: Long,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val reason: String? = null
)

