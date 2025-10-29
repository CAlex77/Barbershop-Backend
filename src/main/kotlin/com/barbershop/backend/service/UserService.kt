package com.barbershop.backend.service

import com.barbershop.backend.dto.request.UserRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.UserResponse
import com.barbershop.backend.entity.User
import com.barbershop.backend.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Suppress("unused")
class UserService(
    private val userRepository: UserRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?): PagedResponse<UserResponse> {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = userRepository.findAll(pageable)
        return PagedResponse(
            content = pageRes.content.map { it.toResponse() },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long): UserResponse? = userRepository.findById(id).map { it.toResponse() }.orElse(null)

    fun create(req: UserRequest): UserResponse {
        val saved = userRepository.save(
            User(name = req.name, email = req.email, phone = req.phone, role = req.role)
        )
        return saved.toResponse()
    }

    fun update(id: Long, req: UserRequest): UserResponse? {
        val maybe = userRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply {
            name = req.name
            email = req.email
            phone = req.phone
            role = req.role
        }
        return userRepository.save(entity).toResponse()
    }

    fun delete(id: Long): Boolean {
        if (!userRepository.existsById(id)) return false
        userRepository.deleteById(id)
        return true
    }

    private fun User.toResponse() = UserResponse(
        userId = userId,
        name = name,
        email = email,
        phone = phone,
        role = role
    )
}
