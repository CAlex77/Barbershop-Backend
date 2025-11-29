package com.barbershop.backend.service

import com.barbershop.backend.dto.response.AvailabilitySlotResponse
import com.barbershop.backend.repository.AppointmentRepository
import com.barbershop.backend.repository.BlockedSlotRepository
import com.barbershop.backend.repository.ServiceRepository
import org.springframework.stereotype.Service
import java.time.*

@Service
class AvailabilityService(
    private val workingHourService: WorkingHourService,
    private val appointmentRepository: AppointmentRepository,
    private val blockedSlotRepository: BlockedSlotRepository,
    private val serviceRepository: ServiceRepository
) {
    fun getAvailability(
        barberId: Long,
        serviceId: Long,
        date: LocalDate,
        zone: ZoneId = ZoneId.of("America/Manaus")
    ): List<AvailabilitySlotResponse> {
        val service = serviceRepository.findById(serviceId).orElseThrow { IllegalArgumentException("Serviço inexistente") }
        val durationMinutes = service.durationMinutes
        // DB usa 0..6 (0=domingo). LocalDate.dayOfWeek.value é 1..7 (7=domingo).
        val dayOfWeekPg = date.dayOfWeek.value % 7

        val workingHours = workingHourService.findByBarberAndDayOfWeek(barberId, dayOfWeekPg)
        if (workingHours.isEmpty()) return emptyList()

        val result = mutableListOf<AvailabilitySlotResponse>()

        val appointments = appointmentRepository.findByBarberId(barberId)
        val blocked = blockedSlotRepository.findByBarberId(barberId)

        workingHours.forEach { wh ->
            // WorkingHourResponse usa LocalTime; compor com LocalDate + zone
            val startZdt = ZonedDateTime.of(date, wh.startTime, zone).toOffsetDateTime()
            val endZdt = ZonedDateTime.of(date, wh.endTime, zone).toOffsetDateTime()

            var cursor = startZdt
            val step = Duration.ofMinutes(durationMinutes.toLong())
            while (cursor.plus(step) <= endZdt) {
                val slotEnd = cursor.plus(step)

                val overlapsAppt = appointments.any { appt ->
                    val apptStart = appt.startTime
                    val apptEnd = appt.endTime ?: apptStart.plusMinutes(durationMinutes.toLong())
                    cursor.isBefore(apptEnd) && slotEnd.isAfter(apptStart)
                }

                val overlapsBlocked = blocked.any { b ->
                    cursor.toInstant().isBefore(b.endTime.toInstant()) && slotEnd.toInstant().isAfter(b.startTime.toInstant())
                }

                if (!overlapsAppt && !overlapsBlocked) {
                    result += AvailabilitySlotResponse(cursor, slotEnd)
                }
                cursor = slotEnd
            }
        }

        return result.sortedBy { it.start }.distinctBy { it.start }
    }
}
