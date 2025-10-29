package com.barbershop.backend.service

import com.barbershop.backend.dto.request.BlockedSlotRequest
import com.barbershop.backend.dto.response.BlockedSlotResponse
import com.barbershop.backend.repository.BlockedSlotRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class BlockedSlotService(
    private val blockedSlotRepository: BlockedSlotRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?) = run {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = blockedSlotRepository.findAll(pageable)
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

    fun get(id: Long) = blockedSlotRepository.findById(id).map { it.toResponse() }.orElse(null)

    fun listByBarber(barberId: Long) = blockedSlotRepository.findByBarberId(barberId).map { it.toResponse() }

    fun create(req: BlockedSlotRequest): BlockedSlotResponse {
        val saved = blockedSlotRepository.save(
            com.barbershop.backend.entity.BlockedSlot(
                barberId = req.barberId,
                startTime = req.startTime,
                endTime = req.endTime,
                reason = req.reason
            )
        )
        return saved.toResponse()
    }

    fun update(id: Long, req: BlockedSlotRequest): BlockedSlotResponse? {
        val maybe = blockedSlotRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            barberId = req.barberId
            startTime = req.startTime
            endTime = req.endTime
            reason = req.reason
        }
        return blockedSlotRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        if (!blockedSlotRepository.existsById(id)) return false
        blockedSlotRepository.deleteById(id)
        return true
    }

    private fun com.barbershop.backend.entity.BlockedSlot.toResponse() = BlockedSlotResponse(
        blockedSlotId = blockedSlotId,
        barberId = barberId,
        startTime = startTime,
        endTime = endTime,
        reason = reason
    )
}

