package com.barbershop.backend.dto.response

data class UserResponse(
    val userId: Long?,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val avatarUrl: String?,
    // New fields: id of client or barber related to this user (only one will be non-null)
    val clientId: Long? = null,
    val barberId: Long? = null
)
