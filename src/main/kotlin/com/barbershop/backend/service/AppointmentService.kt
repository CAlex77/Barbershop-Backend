package com.barbershop.backend.service

import com.barbershop.backend.dto.request.AppointmentRequest
import com.barbershop.backend.dto.request.BookAppointmentRequest
import com.barbershop.backend.dto.request.RescheduleAppointmentRequest
import com.barbershop.backend.dto.response.AppointmentResponse
import com.barbershop.backend.dto.response.BookedAppointmentResponse
import com.barbershop.backend.repository.AppointmentNativeRepository
import com.barbershop.backend.repository.AppointmentRepository
import com.barbershop.backend.repository.ClientRepository
import com.barbershop.backend.repository.BarberRepository
import com.barbershop.backend.repository.UserRepository
import com.barbershop.backend.repository.ServiceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val appointmentNativeRepository: AppointmentNativeRepository,
    private val clientRepository: ClientRepository,
    private val barberRepository: BarberRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository
) {
    // Parse incoming startTime string into an Instant.
    // Accepts either an ISO instant (ending with 'Z' or offset) or a local ISO date-time (e.g. "2025-11-27T09:55:00").
    // When a local date-time is provided, `tz` is used to interpret it (defaults to America/Manaus if null).
    private fun parseStartToInstant(startTimeStr: String, tz: String?): Instant {
        // Try parsing as UTC instant with Z
        try {
            return Instant.parse(startTimeStr)
        } catch (_: DateTimeParseException) {
            // not an instant in Z
        }

        // Try parsing as OffsetDateTime (e.g. 2025-12-05T09:00:00-03:00 or 2025-12-05T09:00-03:00)
        try {
            val odt = OffsetDateTime.parse(startTimeStr)
            return odt.toInstant()
        } catch (_: DateTimeParseException) {
            // not an offset datetime
        }

        // Fallback: parse as LocalDateTime (no offset) and apply provided timezone (or Manaus)
        val ldt = LocalDateTime.parse(startTimeStr)
        val zone = try {
            if (!tz.isNullOrBlank()) ZoneId.of(tz) else ZoneId.of("America/Manaus")
        } catch (_: Exception) {
            // if invalid tz provided, fall back to Manaus
            ZoneId.of("America/Manaus")
        }
        return ldt.atZone(zone).toInstant()
    }

    // Convert AppointmentResponse times to the requested tz (if provided)
    private fun convertAppointmentToTz(appt: AppointmentResponse, tz: String?): AppointmentResponse {
        if (tz.isNullOrBlank()) return appt
        val zone = try { ZoneId.of(tz) } catch (_: Exception) { return appt }
        val start = appt.startTime.toInstant().atZone(zone).toOffsetDateTime()
        val end = appt.endTime?.toInstant()?.atZone(zone)?.toOffsetDateTime()
        return appt.copy(startTime = start, endTime = end)
    }

    // Convert BookedAppointmentResponse times to requested tz
    private fun convertBookedToTz(booked: BookedAppointmentResponse, tz: String?): BookedAppointmentResponse {
        if (tz.isNullOrBlank()) return booked
        val zone = try { ZoneId.of(tz) } catch (_: Exception) { return booked }
        val start = booked.start.toInstant().atZone(zone).toOffsetDateTime()
        val end = booked.end.toInstant().atZone(zone).toOffsetDateTime()
        return booked.copy(start = start, end = end)
    }

    fun list(page: Int, size: Int, sort: String?, dir: String?, tz: String? = null) = run {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = appointmentRepository.findAll(pageable)
        com.barbershop.backend.dto.response.PagedResponse(
            content = pageRes.content.map { convertAppointmentToTz(it.toResponse(), tz) },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long, tz: String? = null): AppointmentResponse? =
        appointmentRepository.findById(id).map { convertAppointmentToTz(it.toResponse(), tz) }.orElse(null)

    fun listByBarber(barberId: Long, tz: String? = null) = appointmentRepository.findByBarberId(barberId).map { convertAppointmentToTz(it.toResponse(), tz) }

    fun listByClient(clientId: Long, tz: String? = null) = appointmentRepository.findByClientId(clientId).map { convertAppointmentToTz(it.toResponse(), tz) }

    /**
     * Return appointment if it belongs to the authenticated user.
     * If role indicates barber, use barber id linked to the user; otherwise use client id.
     */
    fun getForUser(appointmentId: Long, userId: Long, role: String, tz: String? = null): AppointmentResponse? {
        val apptEntity = appointmentRepository.findById(appointmentId).orElse(null) ?: return null
        val roleLower = role.lowercase()
        return if (roleLower.contains("barber") || roleLower.contains("barbeiro") || roleLower.contains("barber")) {
            val barber = barberRepository.findByUserId(userId) ?: return null
            val barberId = barber.barberId ?: return null
            if (apptEntity.barberId == barberId) convertAppointmentToTz(apptEntity.toResponse(), tz) else null
        } else {
            val client = clientRepository.findByUserId(userId) ?: return null
            val clientId = client.clientId ?: return null
            if (apptEntity.clientId == clientId) convertAppointmentToTz(apptEntity.toResponse(), tz) else null
        }
    }

    // New: list all appointments for the authenticated user (by clientId or barberId)
    fun listForUser(userId: Long, role: String, tz: String? = null): List<AppointmentResponse> {
        val roleLower = role.lowercase()
        return if (roleLower.contains("barber") || roleLower.contains("barbeiro") || roleLower.contains("barber")) {
            val barber = barberRepository.findByUserId(userId) ?: return emptyList()
            val barberId = barber.barberId ?: return emptyList()
            appointmentRepository.findByBarberId(barberId).map { convertAppointmentToTz(it.toResponse(), tz) }
        } else {
            val client = clientRepository.findByUserId(userId) ?: return emptyList()
            val clientId = client.clientId ?: return emptyList()
            appointmentRepository.findByClientId(clientId).map { convertAppointmentToTz(it.toResponse(), tz) }
        }
    }

    fun create(req: AppointmentRequest): AppointmentResponse {
        val saved = appointmentRepository.save(
            com.barbershop.backend.entity.Appointment(
                barberId = req.barberId,
                serviceId = req.serviceId,
                clientId = req.clientId,
                startTime = req.startTime,
                status = req.status ?: "SCHEDULED"
            )
        )
        return saved.toResponse()
    }

    fun update(id: Long, req: AppointmentRequest): AppointmentResponse? {
        val maybe = appointmentRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            barberId = req.barberId
            serviceId = req.serviceId
            clientId = req.clientId
            startTime = req.startTime
            status = req.status ?: status
        }
        return appointmentRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        if (!appointmentRepository.existsById(id)) return false
        appointmentRepository.deleteById(id)
        return true
    }

    private fun com.barbershop.backend.entity.Appointment.toResponse() = AppointmentResponse(
        appointmentId = appointmentId,
        barberId = barberId,
        serviceId = serviceId,
        clientId = clientId,
        startTime = startTime,
        endTime = endTime,
        status = status,
        totalPrice = totalPrice,
        // Resolve names where possible; fall back to null when not found
        clientName = run {
            val client = clientRepository.findById(clientId).orElse(null) ?: return@run null
            val user = client.userId.let { uid -> userRepository.findById(uid).orElse(null) } ?: return@run null
            user.name
        },
        barberName = barberRepository.findById(barberId).orElse(null)?.name,
        serviceName = serviceRepository.findById(serviceId).orElse(null)?.name
    )

    /**
     * Reserva usando função SQL book_appointment para garantir atomicidade e cálculo correto de end_time.
     */
    fun book(req: BookAppointmentRequest): BookedAppointmentResponse {
        val startInstant = parseStartToInstant(req.startTime, req.tz)
        val booked = appointmentNativeRepository.bookAppointment(
            clientId = req.clientId,
            barberId = req.barberId,
            serviceId = req.serviceId,
            startUtc = startInstant,
            tz = req.tz ?: "America/Manaus"
        )
        // convert returned times to requested tz for clarity in the client
        return convertBookedToTz(booked, req.tz ?: "America/Manaus")
    }

    /**
     * Reschedule an appointment for the authenticated user.
     * Validates ownership and slot availability before updating.
     */
    fun rescheduleForUser(
        appointmentId: Long,
        userId: Long,
        role: String,
        req: RescheduleAppointmentRequest,
        tz: String? = null
    ): AppointmentResponse? {
        val apptEntity = appointmentRepository.findById(appointmentId).orElse(null) ?: return null

        // Verify ownership
        val hasPermission = checkOwnership(apptEntity, userId, role)
        if (!hasPermission) {
            throw IllegalArgumentException("Você não tem permissão para reagendar este agendamento")
        }

        // Check if appointment can be rescheduled
        if (apptEntity.status !in listOf("SCHEDULED", "CONFIRMED")) {
            throw IllegalArgumentException("Apenas agendamentos confirmados ou agendados podem ser reagendados (status atual: ${apptEntity.status})")
        }

        val startInstant = parseStartToInstant(req.startTime, req.tz)

        // Validate new slot availability (excluding current appointment)
        val validation = appointmentNativeRepository.validateSlotForReschedule(
            appointmentId = appointmentId,
            barberId = req.barberId,
            serviceId = req.serviceId,
            startUtc = startInstant,
            tz = req.tz ?: "America/Manaus"
        )

        // Update appointment with new details
        apptEntity.apply {
            barberId = req.barberId
            serviceId = req.serviceId
            startTime = startInstant.atOffset(java.time.ZoneOffset.UTC)
            endTime = validation.endTime
            totalPrice = validation.totalPrice
        }

        val saved = appointmentRepository.save(apptEntity).toResponse()
        return convertAppointmentToTz(saved, tz)
    }

    /**
     * Cancel an appointment for the authenticated user.
     * Soft deletes by changing status to CANCELLED.
     */
    fun cancelForUser(appointmentId: Long, userId: Long, role: String): Boolean {
        val apptEntity = appointmentRepository.findById(appointmentId).orElse(null) ?: return false

        // Verify ownership (both client and barber can cancel)
        val hasPermission = checkOwnership(apptEntity, userId, role)
        if (!hasPermission) {
            throw IllegalArgumentException("Você não tem permissão para cancelar este agendamento")
        }

        // Check if appointment can be cancelled
        if (apptEntity.status in listOf("CANCELLED", "COMPLETED", "NO_SHOW")) {
            throw IllegalArgumentException("Este agendamento não pode ser cancelado (status atual: ${apptEntity.status})")
        }

        // Soft delete by updating status
        apptEntity.status = "CANCELLED"
        appointmentRepository.save(apptEntity)
        return true
    }

    /**
     * Helper to check if user owns the appointment.
     */
    private fun checkOwnership(appointment: com.barbershop.backend.entity.Appointment, userId: Long, role: String): Boolean {
        val roleLower = role.lowercase()
        return if (roleLower.contains("barber") || roleLower.contains("barbeiro")) {
            val barber = barberRepository.findByUserId(userId) ?: return false
            val barberId = barber.barberId ?: return false
            appointment.barberId == barberId
        } else {
            val client = clientRepository.findByUserId(userId) ?: return false
            val clientId = client.clientId ?: return false
            appointment.clientId == clientId
        }
    }
}
