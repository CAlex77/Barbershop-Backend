package com.barbershop.backend.service

import com.barbershop.backend.dto.request.ClientRequest
import com.barbershop.backend.dto.response.ClientResponse
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.entity.Client
import com.barbershop.backend.repository.ClientRepository
import com.barbershop.backend.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Suppress("unused")
class ClientService(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository
) {
    fun list(page: Int, size: Int, sort: String?, dir: String?): PagedResponse<ClientResponse> {
        val direction = if (dir?.equals("desc", true) == true) Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = if (!sort.isNullOrBlank()) PageRequest.of(page, size, Sort.by(direction, sort)) else PageRequest.of(page, size)
        val pageRes = clientRepository.findAll(pageable)
        val usersById = userRepository.findAll().associateBy { it.userId }
        return PagedResponse(
            content = pageRes.content.map { c ->
                val user = usersById[c.userId]
                ClientResponse(c.clientId, c.userId, user?.name, user?.email)
            },
            page = pageRes.number,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages,
            sort = sort,
            dir = dir
        )
    }

    fun get(id: Long): ClientResponse? {
        val c = clientRepository.findById(id).orElse(null) ?: return null
        val user = userRepository.findById(c.userId).orElse(null)
        return ClientResponse(c.clientId, c.userId, user?.name, user?.email)
    }

    fun create(req: ClientRequest): ClientResponse {
        val saved = clientRepository.save(Client(userId = req.userId))
        val user = userRepository.findById(saved.userId).orElse(null)
        return ClientResponse(saved.clientId, saved.userId, user?.name, user?.email)
    }

    fun update(id: Long, req: ClientRequest): ClientResponse? {
        val maybe = clientRepository.findById(id)
        if (maybe.isEmpty) return null
        val entity = maybe.get().apply { userId = req.userId }
        val saved = clientRepository.save(entity)
        val user = userRepository.findById(saved.userId).orElse(null)
        return ClientResponse(saved.clientId, saved.userId, user?.name, user?.email)
    }

    fun delete(id: Long): Boolean {
        if (!clientRepository.existsById(id)) return false
        clientRepository.deleteById(id)
        return true
    }
}
