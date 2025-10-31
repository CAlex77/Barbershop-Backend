package com.barbershop.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "appointments")
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    var appointmentId: Long? = null,

    @Column(name = "barber_id", nullable = false)
    var barberId: Long = 0,

    @Column(name = "service_id", nullable = false)
    var serviceId: Long = 0,

    @Column(name = "client_id", nullable = false)
    var clientId: Long = 0,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "end_time")
    var endTime: OffsetDateTime? = null,

    @Column(nullable = false)
    var status: String = "SCHEDULED",

    @Column(name = "total_price")
    var totalPrice: BigDecimal? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Version
    var version: Long? = 0
)
