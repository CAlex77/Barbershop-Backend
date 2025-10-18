package com.barbershop.backend.mapper

import com.barbershop.backend.dto.response.AppointmentResponse
import com.barbershop.backend.dto.response.AvailabilitySlotResponse
import com.barbershop.backend.entity.Appointment
import com.barbershop.backend.repository.AppointmentRepository

fun Appointment.toResponse(): AppointmentResponse = AppointmentResponse(
    id = this.id!!,
    clientId = this.client.id!!,
    barberId = this.barber.id!!,
    serviceId = this.service.id!!,
    startTime = this.startTime,
    endTime = this.endTime,
    status = this.status,
    totalPrice = this.totalPrice
)

fun AppointmentRepository.AvailabilitySlotProjection.toResponse(): AvailabilitySlotResponse =
    AvailabilitySlotResponse(
        slotStart = this.getSlotStart(),
        slotEnd = this.getSlotEnd()
    )

