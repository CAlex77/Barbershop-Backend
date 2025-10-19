package com.barbershop.backend.repository

import com.barbershop.backend.entity.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {
    @Suppress("unused")
    fun findByBarberId(barberId: Long): List<Appointment>

    @Suppress("unused")
    fun findByClientId(clientId: Long): List<Appointment>
}
