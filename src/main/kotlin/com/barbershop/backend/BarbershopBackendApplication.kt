package com.barbershop.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.slf4j.LoggerFactory

@SpringBootApplication(scanBasePackages = ["com.barbershop"])
class BarbershopBackendApplication

fun main(args: Array<String>) {
    // As variáveis de ambiente serão fornecidas pelo ambiente/IDE (EnvFile),
    // por isso não carregamos `.env` aqui para evitar dependências extras.
    val ctx = runApplication<BarbershopBackendApplication>(*args)

    // Depois que a aplicação sobe, recuperamos propriedades úteis para montar os endpoints
    val env = ctx.environment
    val port = env.getProperty("local.server.port") ?: env.getProperty("server.port") ?: "8080"
    val contextPath = env.getProperty("server.servlet.context-path") ?: ""
    val baseUrl = "http://localhost:$port$contextPath/api/v1"

    val log = LoggerFactory.getLogger(BarbershopBackendApplication::class.java)
    log.info("""

    ----------------------------------------------------------
    Application started successfully! Some testable endpoints:
      Users:        $baseUrl/users
      Services:     $baseUrl/services
      Appointments: $baseUrl/appointments

    You can open them in the browser or use the included `test-requests.http` file to exercise the API.
    ----------------------------------------------------------
    """.trimIndent())
}
