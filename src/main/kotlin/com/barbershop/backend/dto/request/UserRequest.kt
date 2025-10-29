package com.barbershop.backend.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request payload for creating/updating a User.
 * We intentionally omit password handling here to avoid exposing hashing concerns at the API layer.
 */
data class UserRequest(
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String?,
    val phone: String?,
    @field:NotBlank
    val role: String
)

