package com.barbershop.backend.dto.request

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    // Optional role: if provided, registration will create the user with this role (e.g. "barber").
    // If omitted, defaults to "client".
    val role: String? = null
)
