package com.barbershop.backend.dto.request

import java.math.BigDecimal

data class ServiceRequest(
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int,
    val isActive: Boolean = true
)

