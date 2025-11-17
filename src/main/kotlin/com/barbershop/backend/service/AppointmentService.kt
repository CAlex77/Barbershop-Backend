package com.barbershop.backend.service

import com.barbershop.backend.dto.request.AppointmentRequest
import com.barbershop.backend.dto.request.BookAppointmentRequest
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

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val appointmentNativeRepository: AppointmentNativeRepository,
    private val clientRepository: ClientRepository,
    private val barberRepository: BarberRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?) = run {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = appointmentRepository.findAll(pageable)
        com.barbershop.backend.dto.response.PagedResponse(
            content = pageRes.content.map { it.toResponse() },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long): AppointmentResponse? = appointmentRepository.findById(id).map { it.toResponse() }.orElse(null)

    fun listByBarber(barberId: Long) = appointmentRepository.findByBarberId(barberId).map { it.toResponse() }

    fun listByClient(clientId: Long) = appointmentRepository.findByClientId(clientId).map { it.toResponse() }

    /**
     * Return appointment if it belongs to the authenticated user.
     * If role indicates barber, use barber id linked to the user; otherwise use client id.
     */
    fun getForUser(appointmentId: Long, userId: Long, role: String): AppointmentResponse? {
        val apptEntity = appointmentRepository.findById(appointmentId).orElse(null) ?: return null
        val roleLower = role.lowercase()
        return if (roleLower.contains("barber") || roleLower.contains("barbeiro") || roleLower.contains("barber")) {
            val barber = barberRepository.findByUserId(userId) ?: return null
            val barberId = barber.barberId ?: return null
            if (apptEntity.barberId == barberId) apptEntity.toResponse() else null
        } else {
            val client = clientRepository.findByUserId(userId) ?: return null
            val clientId = client.clientId ?: return null
            if (apptEntity.clientId == clientId) apptEntity.toResponse() else null
        }
    }

    // New: list all appointments for the authenticated user (by clientId or barberId)
    fun listForUser(userId: Long, role: String): List<AppointmentResponse> {
        val roleLower = role.lowercase()
        return if (roleLower.contains("barber") || roleLower.contains("barbeiro") || roleLower.contains("barber")) {
            val barber = barberRepository.findByUserId(userId) ?: return emptyList()
            val barberId = barber.barberId ?: return emptyList()
            appointmentRepository.findByBarberId(barberId).map { it.toResponse() }
        } else {
            val client = clientRepository.findByUserId(userId) ?: return emptyList()
            val clientId = client.clientId ?: return emptyList()
            appointmentRepository.findByClientId(clientId).map { it.toResponse() }
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
        val startInstant = Instant.parse(req.startTime)
        return appointmentNativeRepository.bookAppointment(
            clientId = req.clientId,
            barberId = req.barberId,
            serviceId = req.serviceId,
            startUtc = startInstant,
            tz = req.tz ?: "America/Sao_Paulo"
        )
    }
}
