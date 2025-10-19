// Repositório JPA para Barber
package com.barbershop.backend.repository

import com.barbershop.backend.entity.Barber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarberRepository : JpaRepository<Barber, Long> {
    fun findByUserId(userId: Long): Barber?
}

