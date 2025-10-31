package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class BlockedSlotRequest(
    @field:NotNull
    var barberId: Long,
    @field:NotNull
    var startTime: OffsetDateTime,
    @field:NotNull
    var endTime: OffsetDateTime,
    val reason: String? = null
)
