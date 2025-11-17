package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.AppointmentRequest
import com.barbershop.backend.dto.response.AppointmentResponse
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.service.AppointmentService
import com.barbershop.backend.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class AppointmentsController(
    private val appointmentService: AppointmentService
) {

    @GetMapping("/appointments", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?
    ): PagedResponse<AppointmentResponse> = appointmentService.list(page, size, sort, dir)

    @GetMapping("/appointments/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<AppointmentResponse> =
        appointmentService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @GetMapping("/appointments/by_barber", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByBarber(@RequestParam barberId: Long): List<AppointmentResponse> =
        appointmentService.listByBarber(barberId)

    @GetMapping("/appointments/by_client", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByClient(@RequestParam clientId: Long): List<AppointmentResponse> =
        appointmentService.listByClient(clientId)

    @GetMapping("/appointments/me", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listForMe(@AuthenticationPrincipal principal: UserPrincipal?): ResponseEntity<List<AppointmentResponse>> {
        if (principal == null) return ResponseEntity.status(401).build()
        val res = appointmentService.listForUser(principal.userId, principal.role)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/appointments/me/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getForMe(@PathVariable id: Long, @AuthenticationPrincipal principal: UserPrincipal?): ResponseEntity<AppointmentResponse> {
        if (principal == null) return ResponseEntity.status(401).build()
        val res = appointmentService.getForUser(id, principal.userId, principal.role)
        return res?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping(
        "/appointments",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(@RequestBody @Valid req: AppointmentRequest): ResponseEntity<AppointmentResponse> {
        val saved = appointmentService.create(req)
        val location = URI.create("/api/v1/appointments/${saved.appointmentId}")
        return ResponseEntity.created(location).body(saved)
    }

    @PutMapping(
        "/appointments/{id}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun update(@PathVariable id: Long, @RequestBody @Valid req: AppointmentRequest): ResponseEntity<AppointmentResponse> =
        appointmentService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/appointments/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (appointmentService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
