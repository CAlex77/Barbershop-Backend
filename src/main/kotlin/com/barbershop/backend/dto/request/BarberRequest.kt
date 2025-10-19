package com.barbershop.backend.dto.request

data class BarberRequest(
    val userId: Long,
    val name: String,
    val phone: String?
)

