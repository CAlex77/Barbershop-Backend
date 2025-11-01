package com.barbershop.backend.controller

import com.barbershop.backend.dto.request.LoginRequest
import com.barbershop.backend.dto.request.RegisterRequest
import com.barbershop.backend.dto.response.AuthResponse
import com.barbershop.backend.dto.response.MeResponse
import com.barbershop.backend.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<MeResponse> {
        try {
            val created = authService.register(req)
            return ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        try {
            val resp = authService.login(req)
            return ResponseEntity.ok(resp)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.message)
        }
    }
}
