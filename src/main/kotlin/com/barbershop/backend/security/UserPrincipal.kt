package com.barbershop.backend.security

import com.barbershop.backend.entity.User

data class UserPrincipal(
    val userId: Long,
    val email: String?,
    val name: String,
    val role: String
) {
    companion object {
        fun fromUser(user: User) = UserPrincipal(
            userId = user.userId ?: -1,
            email = user.email,
            name = user.name,
            role = user.role
        )
    }
}

