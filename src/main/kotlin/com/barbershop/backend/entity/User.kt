// Criando entidade User baseada na migration V1__init_schema.sql
package com.barbershop.backend.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(unique = true)
    var email: String? = null,

    @Column(name = "password_hash")
    var passwordHash: String? = null,

    var phone: String? = null,

    var role: String = "client",

    @Column(name = "avatar_path")
    var avatarPath: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)
