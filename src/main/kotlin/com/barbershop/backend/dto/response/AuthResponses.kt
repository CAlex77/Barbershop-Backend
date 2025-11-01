package com.barbershop.backend.dto.response

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer"
)

data class MeResponse(
    val userId: Long?,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String
)

