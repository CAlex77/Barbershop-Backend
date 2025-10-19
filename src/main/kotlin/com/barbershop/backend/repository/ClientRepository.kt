// Repositório JPA para Client
package com.barbershop.backend.repository

import com.barbershop.backend.entity.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : JpaRepository<Client, Long> {
    fun findByUserId(userId: Long): Client?
}

