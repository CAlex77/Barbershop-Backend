package com.barbershop.backend.service

import com.barbershop.backend.dto.request.BarberRequest
import com.barbershop.backend.dto.response.BarberResponse
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.entity.Barber
import com.barbershop.backend.repository.BarberRepository
import com.barbershop.backend.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Suppress("unused")
class BarberService(
    private val barberRepository: BarberRepository,
    private val userRepository: UserRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?, q: String? = null, active: Boolean? = true): PagedResponse<BarberResponse> {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = barberRepository.findAll(pageable)
        val usersById = userRepository.findAll().associateBy { it.userId }
        return PagedResponse(
            content = pageRes.content.filter { b ->
                val u = usersById[b.userId]
                (active == null || b.isActive == active) && (q.isNullOrBlank() || (b.name.contains(q, true) || u?.name?.contains(q, true) == true))
            }.map { b ->
                val u = usersById[b.userId]
                BarberResponse(b.barberId, b.userId, b.name, b.phone, u?.name, u?.email)
            },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long): BarberResponse? {
        val b = barberRepository.findById(id).orElse(null) ?: return null
        if (!b.isActive) return null
        val u = userRepository.findById(b.userId).orElse(null)
        return BarberResponse(b.barberId, b.userId, b.name, b.phone, u?.name, u?.email)
    }

    fun create(req: BarberRequest): BarberResponse {
        val saved = barberRepository.save(Barber(userId = req.userId, name = req.name, phone = req.phone, isActive = true))
        val u = userRepository.findById(saved.userId).orElse(null)
        return BarberResponse(saved.barberId, saved.userId, saved.name, saved.phone, u?.name, u?.email)
    }

    fun update(id: Long, req: BarberRequest): BarberResponse? {
        val maybe = barberRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            userId = req.userId
            name = req.name
            phone = req.phone
        }
        val saved = barberRepository.save(entity)
        val u = userRepository.findById(saved.userId).orElse(null)
        return BarberResponse(saved.barberId, saved.userId, saved.name, saved.phone, u?.name, u?.email)
    }

    fun delete(id: Long): Boolean {
        val maybe = barberRepository.findById(id)
        if (maybe.isEmpty) return false
        val entity = maybe.get()
        entity.isActive = false
        barberRepository.save(entity)
        return true
    }
}
