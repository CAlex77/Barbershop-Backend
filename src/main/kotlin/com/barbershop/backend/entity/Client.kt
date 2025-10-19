// Entidade Client mapeando a tabela `clients` da migration
package com.barbershop.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "clients")
data class Client(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    var clientId: Long? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    var userId: Long = 0
)

