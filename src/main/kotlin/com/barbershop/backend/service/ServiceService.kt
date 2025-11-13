package com.barbershop.backend.service

import com.barbershop.backend.dto.request.ServiceRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.ServiceResponse
import com.barbershop.backend.repository.ServiceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.barbershop.backend.entity.Service as ServiceEntity

@Service
@Suppress("unused")
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val imageStorageService: ImageStorageService
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?, active: Boolean? = null, category: String? = null): PagedResponse<ServiceResponse> {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = when {
            active != null && !category.isNullOrBlank() -> serviceRepository.findByIsActiveAndCategoryIgnoreCase(active, category, pageable)
            active != null -> serviceRepository.findByIsActive(active, pageable)
            !category.isNullOrBlank() -> serviceRepository.findByCategoryIgnoreCase(category, pageable)
            else -> serviceRepository.findAll(pageable)
        }
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
        val saved = serviceRepository.save(
            ServiceEntity(name = req.name, price = req.price, durationMinutes = req.durationMinutes, isActive = req.isActive, category = req.category)
        )
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
            category = req.category
        }
        return serviceRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        val maybe = serviceRepository.findById(id)
        if (maybe.isEmpty) return false
        val entity = maybe.get()
        entity.isActive = false
        serviceRepository.save(entity)
        return true
    }

    fun uploadImage(serviceId: Long, file: MultipartFile): ServiceResponse? {
        val entity = serviceRepository.findById(serviceId).orElse(null) ?: return null
        val stored = imageStorageService.save(file)
        entity.imagePath = stored.id
        val saved = serviceRepository.save(entity)
        return saved.toResponse()
    }

    fun getImagePath(serviceId: Long): String? =
        serviceRepository.findById(serviceId).map { it.imagePath }.orElse(null)

    private fun ServiceEntity.toResponse() = ServiceResponse(
        serviceId = serviceId,
        name = name,
        price = price,
        durationMinutes = durationMinutes,
        isActive = isActive,
        imageUrl = serviceId?.let { if (imagePath != null) "/api/v1/services/$it/image" else null }
    )
}
