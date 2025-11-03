package com.barbershop.backend.dto.response

import java.math.BigDecimal

data class ServiceResponse(
    val serviceId: Long?,
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int,
    val isActive: Boolean,
    val imageUrl: String?
)
