package com.barbershop.backend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class WorkingHourRequest(
    @field:NotNull
    var barberId: Long,
    @field:Min(1)
    @field:Max(7)
    val dayOfWeek: Int,
    @field:NotNull
    var startTime: LocalTime,
    @field:NotNull
    var endTime: LocalTime
)
