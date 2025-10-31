// Entidade Barber mapeando a tabela `barbers` da migration
package com.barbershop.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "barbers")
data class Barber(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barber_id")
    var barberId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    var phone: String? = null,

    var isActive: Boolean = true
)
