package com.barbershop.backend.controller.api.v1

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ProductsController {

    @GetMapping("/products", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): ResponseEntity<Map<String, String>> = notImplemented("products")

    @GetMapping("/products/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("products")

    @PostMapping("/products", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(): ResponseEntity<Map<String, String>> = notImplemented("products")

    @PutMapping("/products/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("products")

    @DeleteMapping("/products/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Map<String, String>> = notImplemented("products")

    private fun notImplemented(resource: String) =
        ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(mapOf("message" to "Endpoint '$resource' ainda não implementado: schema do banco não encontrado."))
}
