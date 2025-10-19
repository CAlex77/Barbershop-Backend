package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.response.BarberResponse
import com.barbershop.backend.dto.response.ClientResponse
import com.barbershop.backend.dto.response.UserResponse
import com.barbershop.backend.repository.BarberRepository
import com.barbershop.backend.repository.ClientRepository
import com.barbershop.backend.repository.UserRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UsersController(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val barberRepository: BarberRepository
) {

    @GetMapping("/usuarios", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUsuarios(): List<UserResponse> {
        return userRepository.findAll().map { u ->
            UserResponse(
                userId = u.userId,
                name = u.name,
                email = u.email,
                phone = u.phone,
                role = u.role
            )
        }
    }

    @GetMapping("/clientes", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClientes(): List<ClientResponse> {
        val usersById = userRepository.findAll().associateBy { it.userId }
        return clientRepository.findAll().map { c ->
            val user = usersById[c.userId]
            ClientResponse(
                clientId = c.clientId,
                userId = c.userId,
                userName = user?.name,
                userEmail = user?.email
            )
        }
    }

    @GetMapping("/barbeiros", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getBarbeiros(): List<BarberResponse> {
        val usersById = userRepository.findAll().associateBy { it.userId }
        return barberRepository.findAll().map { b ->
            val user = usersById[b.userId]
            BarberResponse(
                barberId = b.barberId,
                userId = b.userId,
                name = b.name,
                phone = b.phone,
                userName = user?.name,
                userEmail = user?.email
            )
        }
    }
}

