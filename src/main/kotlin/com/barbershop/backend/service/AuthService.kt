package com.barbershop.backend.service

import com.barbershop.backend.dto.request.LoginRequest
import com.barbershop.backend.dto.request.RegisterRequest
import com.barbershop.backend.dto.response.AuthResponse
import com.barbershop.backend.dto.response.MeResponse
import com.barbershop.backend.entity.User
import com.barbershop.backend.repository.UserRepository
import com.barbershop.backend.security.JwtUtil
import com.barbershop.backend.security.UserPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    fun register(req: RegisterRequest): MeResponse {
        val existing = req.email?.let { userRepository.findByEmail(it) }
        if (existing != null) throw IllegalArgumentException("Email already registered")
        val hashed = passwordEncoder.encode(req.password)
        val user = User(name = req.name, email = req.email, passwordHash = hashed, phone = req.phone)
        val saved = userRepository.save(user)
        return MeResponse(saved.userId, saved.name, saved.email, saved.phone, saved.role)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(req.email) ?: throw IllegalArgumentException("Invalid credentials")
        if (user.passwordHash == null || !passwordEncoder.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        val token = jwtUtil.generateToken(user.userId ?: -1, user.email)
        return AuthResponse(token = token)
    }

    fun me(principal: Any?): MeResponse? {
        if (principal is UserPrincipal) {
            val user = userRepository.findById(principal.userId).orElse(null) ?: return null
            return MeResponse(user.userId, user.name, user.email, user.phone, user.role)
        }
        return null
    }
}

