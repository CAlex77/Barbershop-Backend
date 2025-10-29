package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BarberRequest(
    @field:NotNull
    var userId: Long,
    @field:NotBlank
    val name: String,
    val phone: String?
)
