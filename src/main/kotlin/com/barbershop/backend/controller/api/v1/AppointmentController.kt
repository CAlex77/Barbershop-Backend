package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.AppointmentCreateRequest
import com.barbershop.backend.dto.response.AppointmentResponse
import com.barbershop.backend.service.AppointmentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/v1/appointments")
class AppointmentController(
    private val appointmentService: AppointmentService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid req: AppointmentCreateRequest): AppointmentResponse =
        appointmentService.create(req)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): AppointmentResponse =
        appointmentService.get(id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(@PathVariable id: Long) =
        appointmentService.cancel(id)
}
