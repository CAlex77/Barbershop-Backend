package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.response.AvailabilitySlotResponse
import com.barbershop.backend.service.AvailabilityService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/availability")
class AvailabilityController(
    private val availabilityService: AvailabilityService
) {
    @GetMapping
    fun getSlots(
        @RequestParam barberId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam serviceId: Long,
        @RequestParam(required = false) stepMinutes: Int?,
        @RequestParam(required = false) tz: String?
    ): List<AvailabilitySlotResponse> =
        availabilityService.getSlots(barberId, date, serviceId, stepMinutes, tz)
}

