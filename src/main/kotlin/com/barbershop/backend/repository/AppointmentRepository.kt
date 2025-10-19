package com.barbershop.backend.repository

import com.barbershop.backend.entity.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByBarberId(barberId: Long): List<Appointment>
    fun findByClientId(clientId: Long): List<Appointment>
}

