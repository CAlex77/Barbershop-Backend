package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.ServiceRequest
import com.barbershop.backend.dto.response.ServiceResponse
import com.barbershop.backend.entity.Service
import com.barbershop.backend.repository.ServiceRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ServicesController(
    private val serviceRepository: ServiceRepository
) {

    @GetMapping("/services", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<ServiceResponse> = serviceRepository.findAll().map { it.toResponse() }

    @GetMapping("/services/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<ServiceResponse> =
        serviceRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping("/services", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody req: ServiceRequest): ServiceResponse {
        val entity = Service(
            name = req.name,
            price = req.price,
            durationMinutes = req.durationMinutes,
            isActive = req.isActive
        )
        return serviceRepository.save(entity).toResponse()
    }

    @PutMapping("/services/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody req: ServiceRequest): ResponseEntity<ServiceResponse> {
        val maybe = serviceRepository.findById(id)
        if (maybe.isEmpty) return ResponseEntity.notFound().build()
        val entity = maybe.get().apply {
            name = req.name
            price = req.price
            durationMinutes = req.durationMinutes
            isActive = req.isActive
        }
        return ResponseEntity.ok(serviceRepository.save(entity).toResponse())
    }

    @DeleteMapping("/services/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        return if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()
    }

    private fun Service.toResponse() = ServiceResponse(
        serviceId = serviceId,
        name = name,
        price = price,
        durationMinutes = durationMinutes,
        isActive = isActive
    )
}

