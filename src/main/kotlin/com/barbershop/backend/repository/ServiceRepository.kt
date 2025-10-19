package com.barbershop.backend.repository

import com.barbershop.backend.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<Service, Long>

