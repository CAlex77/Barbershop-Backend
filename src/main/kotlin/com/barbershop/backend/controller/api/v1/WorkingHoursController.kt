package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.WorkingHourRequest
import com.barbershop.backend.dto.response.WorkingHourResponse
import com.barbershop.backend.entity.WorkingHour
import com.barbershop.backend.repository.WorkingHourRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class WorkingHoursController(
    private val workingHourRepository: WorkingHourRepository
) {

    @GetMapping("/working_hours", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<WorkingHourResponse> = workingHourRepository.findAll().map { it.toResponse() }

    @GetMapping("/working_hours/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<WorkingHourResponse> =
        workingHourRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/working_hours/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun searchByBarberAndDay(
        @RequestParam barberId: Long,
        @RequestParam dayOfWeek: Int
    ): List<WorkingHourResponse> =
        workingHourRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek).map { it.toResponse() }

    @PostMapping("/working_hours", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody req: WorkingHourRequest): WorkingHourResponse {
        val entity = WorkingHour(
            barberId = req.barberId,
            dayOfWeek = req.dayOfWeek,
            startTime = req.startTime,
            endTime = req.endTime
        )
        return workingHourRepository.save(entity).toResponse()
    }

    @PutMapping("/working_hours/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody req: WorkingHourRequest): ResponseEntity<WorkingHourResponse> {
        val maybe = workingHourRepository.findById(id)
        if (maybe.isEmpty) return ResponseEntity.notFound().build()
        val entity = maybe.get().apply {
            barberId = req.barberId
            dayOfWeek = req.dayOfWeek
            startTime = req.startTime
            endTime = req.endTime
        }
        return ResponseEntity.ok(workingHourRepository.save(entity).toResponse())
    }

    @DeleteMapping("/working_hours/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (workingHourRepository.existsById(id)) {
            workingHourRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()

    private fun WorkingHour.toResponse() = WorkingHourResponse(
        workingHourId = workingHourId,
        barberId = barberId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime
    )
}
