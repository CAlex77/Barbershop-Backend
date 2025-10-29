package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.BlockedSlotRequest
import com.barbershop.backend.dto.response.BlockedSlotResponse
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.service.BlockedSlotService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class BlockedSlotsController(
    private val blockedSlotService: BlockedSlotService
) {

    @GetMapping("/blocked_slots", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?
    ): PagedResponse<BlockedSlotResponse> = blockedSlotService.list(page, size, sort, dir)

    @GetMapping("/blocked_slots/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<BlockedSlotResponse> =
        blockedSlotService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @GetMapping("/blocked_slots/by_barber", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByBarber(@RequestParam barberId: Long): List<BlockedSlotResponse> =
        blockedSlotService.listByBarber(barberId)

    @PostMapping("/blocked_slots", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody @Valid req: BlockedSlotRequest): ResponseEntity<BlockedSlotResponse> {
        val saved = blockedSlotService.create(req)
        return ResponseEntity.created(URI.create("/api/v1/blocked_slots/${saved.blockedSlotId}")).body(saved)
    }

    @PutMapping("/blocked_slots/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody @Valid req: BlockedSlotRequest): ResponseEntity<BlockedSlotResponse> =
        blockedSlotService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/blocked_slots/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (blockedSlotService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
