package com.barbershop.backend.service

import com.barbershop.backend.dto.request.LoginRequest
import com.barbershop.backend.dto.request.RegisterRequest
import com.barbershop.backend.dto.response.AuthResponse
import com.barbershop.backend.dto.response.MeResponse
import com.barbershop.backend.entity.User
import com.barbershop.backend.repository.UserRepository
import com.barbershop.backend.security.JwtUtil
import com.barbershop.backend.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    fun register(req: RegisterRequest): MeResponse {
        // sanitize email and password to avoid accidental spaces or invisible chars
        val emailSanitized = req.email?.trim()
        val incomingPassword = req.password ?: ""
        var sanitized = incomingPassword
            .replace("\uFEFF", "") // BOM
            .replace("\u200B", "") // ZERO WIDTH SPACE
            .replace("\u00A0", "") // NO-BREAK SPACE
            .replace("\u2007", "") // FIGURE SPACE
            .replace("\u202F", "") // NARROW NO-BREAK SPACE
            .replace(Regex("[\r\n\t]"), "")
            .trim()
        sanitized = sanitized.replace(Regex("\\s+"), " ")

        val existing = emailSanitized?.let { userRepository.findByEmail(it) }
        if (existing != null) throw IllegalArgumentException("Email already registered")
        val hashed = passwordEncoder.encode(sanitized)
        val user = User(name = req.name, email = emailSanitized, passwordHash = hashed, phone = req.phone)
        val saved = userRepository.save(user)
        val avatarUrl = if (saved.avatarPath != null && saved.userId != null) "/api/v1/users/${'$'}{saved.userId}/avatar" else null
        return MeResponse(saved.userId, saved.name, saved.email, saved.phone, saved.role, avatarUrl)
    }

    fun login(req: LoginRequest): AuthResponse {
        // sanitize email
        val emailSanitized = req.email?.trim()

        // Log incoming credentials for debugging (temporary) and sanitize password
        val incomingPassword = req.password ?: ""
        // Log raw password at DEBUG and masked at INFO (show what's arriving but avoid full exposure in INFO)
        log.debug("Login attempt for email={} rawPassword='{}'", emailSanitized, incomingPassword)
        val masked = if (incomingPassword.length <= 2) "**" else incomingPassword.first() + "***" + incomingPassword.last()
        log.info("Login attempt for email={} password masked='{}'", emailSanitized, masked)

        // sanitize password: remove common invisible characters and trim
        var sanitized = incomingPassword
            .replace("\uFEFF", "") // BOM
            .replace("\u200B", "") // ZERO WIDTH SPACE
            .replace("\u00A0", "") // NO-BREAK SPACE
            .replace("\u2007", "") // FIGURE SPACE
            .replace("\u202F", "") // NARROW NO-BREAK SPACE
            .replace(Regex("[\r\n\t]"), "")
            .trim()

        // collapse multiple spaces into single spaces (if relevant)
        sanitized = sanitized.replace(Regex("\\s+"), " ")

        // log sanitized in hex for debugging invisible chars (DEBUG only)
        val hex = sanitized.toByteArray().joinToString(separator = " ") { String.format("%02X", it) }
        log.debug("Sanitized password (hex) for email={}: {}", emailSanitized, hex)

        val user = emailSanitized?.let { userRepository.findByEmail(it) } ?: throw IllegalArgumentException("Invalid credentials")
        if (user.passwordHash == null || !passwordEncoder.matches(sanitized, user.passwordHash)) {
            // Log mismatch for debugging
            log.warn("Password mismatch for email={}. Received (sanitized)='{}' (hex={})", emailSanitized, sanitized, hex)
            throw IllegalArgumentException("Invalid credentials")
        }
        val token = jwtUtil.generateToken(user.userId ?: -1, user.email)
        return AuthResponse(token = token)
    }

    fun me(principal: Any?): MeResponse? {
        if (principal is UserPrincipal) {
            val user = userRepository.findById(principal.userId).orElse(null) ?: return null
            val avatarUrl = if (user.avatarPath != null && user.userId != null) "/api/v1/users/${'$'}{user.userId}/avatar" else null
            return MeResponse(user.userId, user.name, user.email, user.phone, user.role, avatarUrl)
        }
        return null
    }
}
