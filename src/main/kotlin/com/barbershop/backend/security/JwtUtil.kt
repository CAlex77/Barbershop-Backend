package com.barbershop.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.Claims
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import java.util.*

@Component
class JwtUtil(
    @Value("\${app.jwtSecret}")
    private val jwtSecret: String = "",
    @Value("\${app.jwtExpirationMs:3600000}")
    private val jwtExpirationMs: Long = 3600000
) {

    private lateinit var signingKey: SecretKey

    @PostConstruct
    fun init() {
        try {
            val keyBytes = try {
                // try base64 decode first
                Decoders.BASE64.decode(jwtSecret)
            } catch (e: Exception) {
                // fallback to raw bytes
                jwtSecret.toByteArray()
            }
            if (keyBytes.isEmpty()) {
                throw IllegalStateException("Missing or empty 'app.jwtSecret' property. Set a base64 or plain secret with sufficient length (>=32 bytes recommended) using your EnvFile or environment variables.")
            }
            signingKey = Keys.hmacShaKeyFor(keyBytes)
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to initialize JWT signing key: ${'$'}{ex.message}", ex)
        }
    }

    private fun key() = signingKey

    fun generateToken(userId: Long, email: String?): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpirationMs)
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .claim("email", email)
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUserIdFromJwtToken(token: String): Long? {
        val claims = parseClaims(token) ?: return null
        val sub = claims.subject ?: return null
        return sub.toLongOrNull()
    }

    fun validateJwtToken(authToken: String): Boolean {
        return try {
            parseClaims(authToken)
            true
        } catch (ex: Exception) {
            false
        }
    }

    private fun parseClaims(token: String): Claims? {
        return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
    }
}
