package com.barbershop.backend.controller

import com.barbershop.backend.dto.response.MeResponse
import com.barbershop.backend.security.UserPrincipal
import com.barbershop.backend.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class MeController(
    private val authService: AuthService
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserPrincipal?): ResponseEntity<MeResponse> {
        val me = authService.me(principal) ?: return ResponseEntity.status(401).build()
        return ResponseEntity.ok(me)
    }
}
