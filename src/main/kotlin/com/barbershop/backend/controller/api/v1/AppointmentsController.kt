package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.AppointmentRequest
import com.barbershop.backend.dto.response.AppointmentResponse
import com.barbershop.backend.entity.Appointment
import com.barbershop.backend.repository.AppointmentRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class AppointmentsController(
    private val appointmentRepository: AppointmentRepository
) {

    @GetMapping("/appointments", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<AppointmentResponse> = appointmentRepository.findAll().map { it.toResponse() }

    @GetMapping("/appointments/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<AppointmentResponse> =
        appointmentRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/appointments/by_barber", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByBarber(@RequestParam barberId: Long): List<AppointmentResponse> =
        appointmentRepository.findByBarberId(barberId).map { it.toResponse() }

    @GetMapping("/appointments/by_client", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByClient(@RequestParam clientId: Long): List<AppointmentResponse> =
        appointmentRepository.findByClientId(clientId).map { it.toResponse() }

    @PostMapping(
        "/appointments",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(@RequestBody req: AppointmentRequest): ResponseEntity<AppointmentResponse> {
        val entity = Appointment(
            barberId = req.barberId,
            serviceId = req.serviceId,
            clientId = req.clientId,
            startTime = req.startTime,
            status = req.status ?: "SCHEDULED"
        )
        val saved = appointmentRepository.save(entity)
        return ResponseEntity.ok(saved.toResponse())
    }

    @PutMapping(
        "/appointments/{id}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun update(@PathVariable id: Long, @RequestBody req: AppointmentRequest): ResponseEntity<AppointmentResponse> {
        val maybe = appointmentRepository.findById(id)
        if (maybe.isEmpty) return ResponseEntity.notFound().build()
        val entity = maybe.get().apply {
            barberId = req.barberId
            serviceId = req.serviceId
            clientId = req.clientId
            startTime = req.startTime
            status = req.status ?: status
        }
        return ResponseEntity.ok(appointmentRepository.save(entity).toResponse())
    }

    @DeleteMapping("/appointments/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()

    private fun Appointment.toResponse() = AppointmentResponse(
        appointmentId = appointmentId,
        barberId = barberId,
        serviceId = serviceId,
        clientId = clientId,
        startTime = startTime,
        endTime = endTime,
        status = status,
        totalPrice = totalPrice
    )
}
