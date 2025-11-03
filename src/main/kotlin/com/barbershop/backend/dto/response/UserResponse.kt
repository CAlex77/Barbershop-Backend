package com.barbershop.backend.dto.response

data class UserResponse(
    val userId: Long?,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val avatarUrl: String?
)
