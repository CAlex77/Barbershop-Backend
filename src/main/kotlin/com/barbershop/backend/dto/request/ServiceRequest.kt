package com.barbershop.backend.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal

data class ServiceRequest(
    @field:NotBlank
    val name: String,
    @field:NotNull
    @field:DecimalMin("0.0")
    var price: BigDecimal,
    @field:Min(1)
    val durationMinutes: Int,
    val isActive: Boolean = true,
    val category: String? = null
)
