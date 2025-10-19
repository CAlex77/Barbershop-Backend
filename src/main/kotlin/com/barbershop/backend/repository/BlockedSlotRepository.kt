package com.barbershop.backend.repository

import com.barbershop.backend.entity.BlockedSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BlockedSlotRepository : JpaRepository<BlockedSlot, Long> {
    fun findByBarberId(barberId: Long): List<BlockedSlot>
}

