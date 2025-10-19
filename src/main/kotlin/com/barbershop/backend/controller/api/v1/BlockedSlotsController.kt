package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.BlockedSlotRequest
import com.barbershop.backend.dto.response.BlockedSlotResponse
import com.barbershop.backend.entity.BlockedSlot
import com.barbershop.backend.repository.BlockedSlotRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class BlockedSlotsController(
    private val blockedSlotRepository: BlockedSlotRepository
) {

    @GetMapping("/blocked_slots", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<BlockedSlotResponse> = blockedSlotRepository.findAll().map { it.toResponse() }

    @GetMapping("/blocked_slots/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<BlockedSlotResponse> =
        blockedSlotRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/blocked_slots/by_barber", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listByBarber(@RequestParam barberId: Long): List<BlockedSlotResponse> =
        blockedSlotRepository.findByBarberId(barberId).map { it.toResponse() }

    @PostMapping("/blocked_slots", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody req: BlockedSlotRequest): BlockedSlotResponse {
        val entity = BlockedSlot(
            barberId = req.barberId,
            startTime = req.startTime,
            endTime = req.endTime,
            reason = req.reason
        )
        return blockedSlotRepository.save(entity).toResponse()
    }

    @PutMapping("/blocked_slots/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody req: BlockedSlotRequest): ResponseEntity<BlockedSlotResponse> {
        val maybe = blockedSlotRepository.findById(id)
        if (maybe.isEmpty) return ResponseEntity.notFound().build()
        val entity = maybe.get().apply {
            barberId = req.barberId
            startTime = req.startTime
            endTime = req.endTime
            reason = req.reason
        }
        return ResponseEntity.ok(blockedSlotRepository.save(entity).toResponse())
    }

    @DeleteMapping("/blocked_slots/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (blockedSlotRepository.existsById(id)) {
            blockedSlotRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()

    private fun BlockedSlot.toResponse() = BlockedSlotResponse(
        blockedSlotId = blockedSlotId,
        barberId = barberId,
        startTime = startTime,
        endTime = endTime,
        reason = reason
    )
}
