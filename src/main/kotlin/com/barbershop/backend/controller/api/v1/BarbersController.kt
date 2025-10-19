package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.BarberRequest
import com.barbershop.backend.dto.response.BarberResponse
import com.barbershop.backend.entity.Barber
import com.barbershop.backend.repository.BarberRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class BarbersController(
    private val barberRepository: BarberRepository
) {

    @GetMapping("/barbers/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<BarberResponse> =
        barberRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping("/barbers", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody req: BarberRequest): BarberResponse {
        val entity = Barber(
            userId = req.userId,
            name = req.name,
            phone = req.phone
        )
        return barberRepository.save(entity).toResponse()
    }

    @PutMapping("/barbers/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody req: BarberRequest): ResponseEntity<BarberResponse> {
        val maybe = barberRepository.findById(id)
        if (maybe.isEmpty) return ResponseEntity.notFound().build()
        val entity = maybe.get().apply {
            userId = req.userId
            name = req.name
            phone = req.phone
        }
        return ResponseEntity.ok(barberRepository.save(entity).toResponse())
    }

    @DeleteMapping("/barbers/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (barberRepository.existsById(id)) {
            barberRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()

    private fun Barber.toResponse() = BarberResponse(
        barberId = barberId,
        userId = userId,
        name = name,
        phone = phone,
        userName = null,   // Enriquecimento com User opcional (mantemos simples aqui)
        userEmail = null
    )
}
