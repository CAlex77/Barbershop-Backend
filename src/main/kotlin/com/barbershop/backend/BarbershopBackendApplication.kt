package com.barbershop.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.barbershop"])
class BarbershopBackendApplication

fun main(args: Array<String>) {
    // As variáveis de ambiente serão fornecidas pelo ambiente/IDE (EnvFile),
    // por isso não carregamos `.env` aqui para evitar dependências extras.
    runApplication<BarbershopBackendApplication>(*args)
}
