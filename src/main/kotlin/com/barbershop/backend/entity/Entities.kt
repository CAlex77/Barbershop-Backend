@file:Suppress("unused")

package com.barbershop.backend.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.LocalTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @Column(unique = true)
    var email: String? = null,
    @Column(name = "password_hash")
    var passwordHash: String? = null,
    var phone: String? = null,
    @Column(nullable = false)
    var role: String = "client",
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Entity
@Table(name = "clients")
class Client(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User
)

@Entity
@Table(name = "barbers")
class Barber(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barber_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var name: String,
    var phone: String? = null
)

@Entity
@Table(name = "services")
class ServiceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int = 30,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
)

@Entity
@Table(name = "appointments")
class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    var barber: Barber,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: ServiceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: Client,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime,

    @Column(name = "end_time")
    var endTime: OffsetDateTime? = null,

    @Column(nullable = false)
    var status: String = "SCHEDULED",

    @Column(name = "total_price")
    var totalPrice: BigDecimal? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Entity
@Table(name = "working_hours")
class WorkingHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "working_hour_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    var barber: Barber,

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime
)

@Entity
@Table(name = "blocked_slots")
class BlockedSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blocked_slot_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    var barber: Barber,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime,

    @Column(name = "end_time", nullable = false)
    var endTime: OffsetDateTime,

    var reason: String? = null
)
