package com.barbershop.backend.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "blocked_slots")
data class BlockedSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blocked_slot_id")
    var blockedSlotId: Long? = null,

    @Column(name = "barber_id", nullable = false)
    var barberId: Long = 0,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "end_time", nullable = false)
    var endTime: OffsetDateTime = OffsetDateTime.now(),

    var reason: String? = null
)

