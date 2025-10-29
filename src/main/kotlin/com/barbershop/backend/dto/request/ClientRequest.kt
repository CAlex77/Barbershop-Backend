package com.barbershop.backend.dto.request

import jakarta.validation.constraints.NotNull

/** Request payload for creating/updating a Client */
data class ClientRequest(
    @field:NotNull
    var userId: Long
)
