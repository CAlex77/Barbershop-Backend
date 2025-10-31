package com.barbershop.backend.repository

import com.barbershop.backend.entity.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<Service, Long> {
    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<Service>
    fun findByCategoryIgnoreCase(category: String, pageable: Pageable): Page<Service>
    fun findByIsActiveAndCategoryIgnoreCase(isActive: Boolean, category: String, pageable: Pageable): Page<Service>
}
