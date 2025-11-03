package com.barbershop.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "services")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    var serviceId: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int = 30,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    var category: String? = null,

    @Column(name = "image_path")
    var imagePath: String? = null
)
