package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.ClientRequest
import com.barbershop.backend.dto.request.UserRequest
import com.barbershop.backend.dto.response.ClientResponse
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.UserResponse
import com.barbershop.backend.service.ClientService
import com.barbershop.backend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class UsersController(
    private val userService: UserService,
    private val clientService: ClientService
) {

    // Users CRUD
    @GetMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?
    ): PagedResponse<UserResponse> = userService.list(page, size, sort, dir)

    @GetMapping("/users/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> =
        userService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping("/users", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createUser(@RequestBody @Valid req: UserRequest): ResponseEntity<UserResponse> {
        val saved = userService.create(req)
        return ResponseEntity.created(URI.create("/api/v1/users/${saved.userId}")).body(saved)
    }

    @PutMapping("/users/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUser(@PathVariable id: Long, @RequestBody @Valid req: UserRequest): ResponseEntity<UserResponse> =
        userService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> =
        if (userService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()

    // Clients CRUD
    @GetMapping("/clients", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listClients(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) active: Boolean?
    ): PagedResponse<ClientResponse> = clientService.list(page, size, sort, dir, q, active ?: true)

    @GetMapping("/clients/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClient(@PathVariable id: Long): ResponseEntity<ClientResponse> =
        clientService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping("/clients", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createClient(@RequestBody @Valid req: ClientRequest): ResponseEntity<ClientResponse> {
        val saved = clientService.create(req)
        val id = saved.clientId
        return ResponseEntity.created(URI.create("/api/v1/clients/$id")).body(saved)
    }

    @PatchMapping("/clients/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClient(@PathVariable id: Long, @RequestBody @Valid req: ClientRequest): ResponseEntity<ClientResponse> =
        clientService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/clients/{id}")
    fun deleteClient(@PathVariable id: Long): ResponseEntity<Void> =
        if (clientService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()
}
