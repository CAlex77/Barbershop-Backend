package com.barbershop.backend.dto.request

import java.time.LocalTime

data class WorkingHourRequest(
    val barberId: Long,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)

