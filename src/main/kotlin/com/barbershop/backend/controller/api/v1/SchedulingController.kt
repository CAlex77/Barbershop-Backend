package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.BookAppointmentRequest
import com.barbershop.backend.dto.response.BookedAppointmentResponse
import com.barbershop.backend.dto.response.AvailabilitySlotResponse
import com.barbershop.backend.service.AppointmentService
import com.barbershop.backend.service.AvailabilityService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1")
class SchedulingController(
    private val availabilityService: AvailabilityService,
    private val appointmentService: AppointmentService
) {

    @GetMapping("/barbers/{barberId}/availability", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun availability(
        @PathVariable barberId: Long,
        @RequestParam serviceId: Long,
        @RequestParam date: String
    ): List<AvailabilitySlotResponse> {
        val localDate = LocalDate.parse(date)
        return availabilityService.getAvailability(barberId, serviceId, localDate)
    }

    @PostMapping("/appointments/book", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun book(@RequestBody @Valid req: BookAppointmentRequest): ResponseEntity<BookedAppointmentResponse> {
        val booked = appointmentService.book(req)
        return ResponseEntity.ok(booked)
    }
}

