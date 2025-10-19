package com.barbershop.backend.repository

import com.barbershop.backend.entity.WorkingHour
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkingHourRepository : JpaRepository<WorkingHour, Long> {
    fun findByBarberIdAndDayOfWeek(barberId: Long, dayOfWeek: Int): List<WorkingHour>
}

