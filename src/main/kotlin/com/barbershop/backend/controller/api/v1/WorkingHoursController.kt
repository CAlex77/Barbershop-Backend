package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.WorkingHourRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.WorkingHourResponse
import com.barbershop.backend.service.WorkingHourService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class WorkingHoursController(
    private val workingHourService: WorkingHourService
) {

    @GetMapping("/working_hours", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?
    ): PagedResponse<WorkingHourResponse> = workingHourService.list(page, size, sort, dir)

    @GetMapping("/working_hours/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<WorkingHourResponse> =
        workingHourService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @GetMapping("/working_hours/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun searchByBarberAndDay(
        @RequestParam barberId: Long,
        @RequestParam dayOfWeek: Int
    ): List<WorkingHourResponse> =
        workingHourService.findByBarberAndDayOfWeek(barberId, dayOfWeek)

    @PostMapping("/working_hours", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody @Valid req: WorkingHourRequest): ResponseEntity<WorkingHourResponse> {
        val saved = workingHourService.create(req)
        return ResponseEntity.created(URI.create("/api/v1/working_hours/${saved.workingHourId}")).body(saved)
    }

    @PutMapping("/working_hours/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody @Valid req: WorkingHourRequest): ResponseEntity<WorkingHourResponse> =
        workingHourService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/working_hours/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (workingHourService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
