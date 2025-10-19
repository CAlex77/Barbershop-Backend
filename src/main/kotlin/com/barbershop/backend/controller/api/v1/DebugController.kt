package com.barbershop.backend.controller.api.v1

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/debug")
class DebugController {

    private fun maskSecret(s: String?): String? {
        if (s.isNullOrBlank()) return null
        if (s.length <= 4) return "****"
        val visibleStart = 2
        val visibleEnd = 2
        val maskedMiddle = "*".repeat(s.length - visibleStart - visibleEnd)
        return s.substring(0, visibleStart) + maskedMiddle + s.substring(s.length - visibleEnd)
    }

    @GetMapping("/env", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEnv(): Map<String, Any?> {
        val dbUrl = System.getProperty("DB_URL") ?: System.getenv("DB_URL")
        val dbUser = System.getProperty("DB_USER") ?: System.getenv("DB_USER")
        val dbPassword = System.getProperty("DB_PASSWORD") ?: System.getenv("DB_PASSWORD")

        return mapOf(
            "dbUrlPresent" to (!dbUrl.isNullOrBlank()),
            "dbUrl" to dbUrl, // URL não contém senha normalmente; exibimos para confirmação
            "dbUserPresent" to (!dbUser.isNullOrBlank()),
            "dbUser" to dbUser,
            "dbPasswordPresent" to (!dbPassword.isNullOrBlank()),
            "dbPasswordMasked" to maskSecret(dbPassword)
        )
    }
}

