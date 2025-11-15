package com.barbershop.backend.service

import com.barbershop.backend.dto.request.UserRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.UserResponse
import com.barbershop.backend.entity.User
import com.barbershop.backend.entity.Client
import com.barbershop.backend.entity.Barber
import com.barbershop.backend.repository.UserRepository
import com.barbershop.backend.repository.ClientRepository
import com.barbershop.backend.repository.BarberRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Suppress("unused")
class UserService(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val barberRepository: BarberRepository,
    private val imageStorageService: ImageStorageService
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

        // create linked client or barber according to role
        val roleLower = req.role.lowercase()
        if (roleLower.contains("barber") || roleLower.contains("barbeiro") || roleLower.contains("barber")) {
            // create barber record if not exists
            val uid = saved.userId ?: 0
            val existing = barberRepository.findByUserId(uid)
            if (existing == null) {
                barberRepository.save(Barber(userId = uid, name = saved.name, phone = saved.phone, isActive = true))
            }
        } else {
            // default: create client
            val uid = saved.userId ?: 0
            val existing = clientRepository.findByUserId(uid)
            if (existing == null) {
                clientRepository.save(Client(userId = uid, isActive = true))
            }
        }

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

    fun uploadAvatar(userId: Long, file: MultipartFile): UserResponse? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val stored = imageStorageService.save(file)
        user.avatarPath = stored.id
        val saved = userRepository.save(user)
        return saved.toResponse()
    }

    fun getAvatarPath(userId: Long): String? =
        userRepository.findById(userId).map { it.avatarPath }.orElse(null)

    private fun User.toResponse(): UserResponse {
        val uid = this.userId
        val client = if (uid != null) clientRepository.findByUserId(uid) else null
        val barber = if (uid != null) barberRepository.findByUserId(uid) else null
        return UserResponse(
            userId = userId,
            name = name,
            email = email,
            phone = phone,
            role = role,
            avatarUrl = if (avatarPath != null && userId != null) "/api/v1/users/${'$'}{userId}/avatar" else null,
            clientId = client?.clientId,
            barberId = barber?.barberId
        )
    }
}
