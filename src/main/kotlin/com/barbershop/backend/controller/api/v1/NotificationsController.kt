package com.barbershop.backend.controller.api.v1

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class NotificationsController {

    @GetMapping("/notifications", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): ResponseEntity<Map<String, String>> = notImplemented("notifications")

    @GetMapping("/notifications/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("notifications")

    @PostMapping("/notifications", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(): ResponseEntity<Map<String, String>> = notImplemented("notifications")

    @PutMapping("/notifications/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("notifications")

    @DeleteMapping("/notifications/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("notifications")

    private fun notImplemented(resource: String) =
        ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(mapOf("message" to "Endpoint '$resource' ainda não implementado: schema do banco não encontrado."))
}

