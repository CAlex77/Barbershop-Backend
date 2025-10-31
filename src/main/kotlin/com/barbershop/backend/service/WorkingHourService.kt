package com.barbershop.backend.service

import com.barbershop.backend.dto.request.WorkingHourRequest
import com.barbershop.backend.dto.response.WorkingHourResponse
import com.barbershop.backend.entity.WorkingHour
import com.barbershop.backend.repository.WorkingHourRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class WorkingHourService(
    private val workingHourRepository: WorkingHourRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?) = run {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = workingHourRepository.findAll(pageable)
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

    fun get(id: Long): WorkingHourResponse? = workingHourRepository.findById(id).map { it.toResponse() }.orElse(null)

    fun findByBarberAndDayOfWeek(barberId: Long, dayOfWeek: Int): List<WorkingHourResponse> =
        workingHourRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek).map { it.toResponse() }

    fun create(req: WorkingHourRequest): WorkingHourResponse {
        val saved = workingHourRepository.save(
            WorkingHour(barberId = req.barberId, dayOfWeek = req.dayOfWeek, startTime = req.startTime, endTime = req.endTime)
        )
        return saved.toResponse()
    }

    fun update(id: Long, req: WorkingHourRequest): WorkingHourResponse? {
        val maybe = workingHourRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            barberId = req.barberId
            dayOfWeek = req.dayOfWeek
            startTime = req.startTime
            endTime = req.endTime
        }
        return workingHourRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        if (!workingHourRepository.existsById(id)) return false
        workingHourRepository.deleteById(id)
        return true
    }

    private fun WorkingHour.toResponse() = WorkingHourResponse(
        workingHourId = workingHourId,
        barberId = barberId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime
    )
}
