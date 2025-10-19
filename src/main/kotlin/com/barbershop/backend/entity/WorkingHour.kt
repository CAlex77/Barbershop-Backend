package com.barbershop.backend.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "working_hours")
data class WorkingHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "working_hour_id")
    var workingHourId: Long? = null,

    @Column(name = "barber_id", nullable = false)
    var barberId: Long = 0,

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 1,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.of(9,0),

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.of(18,0)
)

