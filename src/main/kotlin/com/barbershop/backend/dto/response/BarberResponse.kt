package com.barbershop.backend.dto.response

data class BarberResponse(
    val barberId: Long?,
    val userId: Long,
    val name: String,
    val phone: String?,
    val userName: String?,
    val userEmail: String?
)

