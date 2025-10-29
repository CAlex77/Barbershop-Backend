package com.barbershop.backend.service

import com.barbershop.backend.dto.request.ServiceRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.ServiceResponse
import com.barbershop.backend.entity.Service as ServiceEntity
import com.barbershop.backend.repository.ServiceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Suppress("unused")
class ServiceService(
    private val serviceRepository: ServiceRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?): PagedResponse<ServiceResponse> {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = serviceRepository.findAll(pageable)
        return PagedResponse(
            content = pageRes.content.map { it.toResponse() },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long): ServiceResponse? = serviceRepository.findById(id).map { it.toResponse() }.orElse(null)

    fun create(req: ServiceRequest): ServiceResponse {
        val saved = serviceRepository.save(ServiceEntity(name = req.name, price = req.price, durationMinutes = req.durationMinutes, isActive = req.isActive))
        return saved.toResponse()
    }

    fun update(id: Long, req: ServiceRequest): ServiceResponse? {
        val maybe = serviceRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            name = req.name
            price = req.price
            durationMinutes = req.durationMinutes
            isActive = req.isActive
        }
        return serviceRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        if (!serviceRepository.existsById(id)) return false
        serviceRepository.deleteById(id)
        return true
    }

    private fun ServiceEntity.toResponse() = ServiceResponse(
        serviceId = serviceId,
        name = name,
        price = price,
        durationMinutes = durationMinutes,
        isActive = isActive
    )
}
