package com.barbershop.backend.dto.response

import java.time.LocalTime

data class WorkingHourResponse(
    val workingHourId: Long?,
    val barberId: Long,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)

