package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.AppointmentRequest
import com.barbershop.backend.dto.request.RescheduleAppointmentRequest
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
        @RequestParam(required = false) dir: String?,
        @RequestParam(required = false) tz: String?
    ): PagedResponse<AppointmentResponse> = appointmentService.list(page, size, sort, dir, tz)

    @GetMapping("/appointments/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long, @RequestParam(required = false) tz: String?): ResponseEntity<AppointmentResponse> =
        appointmentService.get(id, tz)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @GetMapping("/appointments/by_barber", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByBarber(@RequestParam barberId: Long, @RequestParam(required = false) tz: String?): List<AppointmentResponse> =
        appointmentService.listByBarber(barberId, tz)

    @GetMapping("/appointments/by_client", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByClient(@RequestParam clientId: Long, @RequestParam(required = false) tz: String?): List<AppointmentResponse> =
        appointmentService.listByClient(clientId, tz)

    @GetMapping("/appointments/me", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listForMe(@AuthenticationPrincipal principal: UserPrincipal?, @RequestParam(required = false) tz: String?): ResponseEntity<List<AppointmentResponse>> {
        if (principal == null) return ResponseEntity.status(401).build()
        // Default to Manaus when client doesn't provide tz to keep frontend display consistent
        val effectiveTz = tz ?: "America/Manaus"
        val res = appointmentService.listForUser(principal.userId, principal.role, effectiveTz)
        return ResponseEntity.ok(res)
    }

    @GetMapping("/appointments/me/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getForMe(@PathVariable id: Long, @AuthenticationPrincipal principal: UserPrincipal?, @RequestParam(required = false) tz: String?): ResponseEntity<AppointmentResponse> {
        if (principal == null) return ResponseEntity.status(401).build()
        val effectiveTz = tz ?: "America/Manaus"
        val res = appointmentService.getForUser(id, principal.userId, principal.role, effectiveTz)
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

    /**
     * Reschedule an appointment for the authenticated user.
     * Validates ownership and slot availability.
     */
    @PutMapping(
        "/appointments/me/{id}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun rescheduleForMe(
        @PathVariable id: Long,
        @RequestBody @Valid req: RescheduleAppointmentRequest,
        @AuthenticationPrincipal principal: UserPrincipal?
        , @RequestParam(required = false) tz: String?
    ): ResponseEntity<AppointmentResponse> {
        if (principal == null) return ResponseEntity.status(401).build()
        val effectiveTz = tz ?: "America/Manaus"
        val updated = appointmentService.rescheduleForUser(id, principal.userId, principal.role, req, effectiveTz)
        return updated?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    /**
     * Cancel an appointment for the authenticated user.
     * Changes status to CANCELLED instead of hard delete.
     */
    @DeleteMapping("/appointments/me/{id}")
    fun cancelForMe(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal?
    ): ResponseEntity<Void> {
        if (principal == null) return ResponseEntity.status(401).build()
        return if (appointmentService.cancelForUser(id, principal.userId, principal.role)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
