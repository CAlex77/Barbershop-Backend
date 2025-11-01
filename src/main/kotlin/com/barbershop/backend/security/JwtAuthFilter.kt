package com.barbershop.backend.security

import com.barbershop.backend.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val header = request.getHeader("Authorization")
            if (!header.isNullOrBlank() && header.startsWith("Bearer ")) {
                val token = header.substringAfter("Bearer ").trim()
                if (jwtUtil.validateJwtToken(token)) {
                    val userId = jwtUtil.getUserIdFromJwtToken(token)
                    if (userId != null) {
                        val userOpt = userRepository.findById(userId)
                        if (userOpt.isPresent) {
                            val user = userOpt.get()
                            val principal = UserPrincipal.fromUser(user)
                            val authorities = listOf(SimpleGrantedAuthority(if (user.role.startsWith("ROLE_")) user.role else "ROLE_${user.role.uppercase()}"))
                            val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
                            SecurityContextHolder.getContext().authentication = auth
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            log.warn("Failed to set user authentication from JWT", ex)
        }

        filterChain.doFilter(request, response)
    }
}

