package com.barbershop.backend.dto.response

data class ClientResponse(
    val clientId: Long?,
    val userId: Long,
    val userName: String?,
    val userEmail: String?
)

