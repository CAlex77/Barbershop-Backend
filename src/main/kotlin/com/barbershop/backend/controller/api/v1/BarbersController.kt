package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.BarberRequest
import com.barbershop.backend.dto.response.BarberResponse
import com.barbershop.backend.service.BarberService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class BarbersController(
    private val barberService: BarberService
) {

    @GetMapping("/barbers", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<BarberResponse> = barberService.list(0, Int.MAX_VALUE, null, null).content

    @GetMapping("/barbers/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<BarberResponse> =
        barberService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping("/barbers", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody @Valid req: BarberRequest): ResponseEntity<BarberResponse> {
        return try {
            val saved = barberService.create(req)
            ResponseEntity.created(URI.create("/api/v1/barbers/${'$'}{saved.barberId}")).body(saved)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/barbers/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody @Valid req: BarberRequest): ResponseEntity<BarberResponse> {
        return try {
            val updated = barberService.update(id, req)
            if (updated == null) ResponseEntity.notFound().build() else ResponseEntity.ok(updated)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/barbers/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (barberService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
