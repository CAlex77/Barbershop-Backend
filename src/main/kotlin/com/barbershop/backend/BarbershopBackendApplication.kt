package com.barbershop.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.barbershop"])
class BarbershopBackendApplication

fun main(args: Array<String>) {
    runApplication<BarbershopBackendApplication>(*args)
}
