package com.barbershop.backend.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
        val status: Int,
        val error: String,
        val message: String?
    )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = "Invalid parameter '${ex.name}': ${ex.message}"
            )
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val first = ex.bindingResult.fieldErrors.firstOrNull()
        val msg = first?.let { "${it.field}: ${it.defaultMessage}" } ?: "Validation error"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = msg
            )
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConstraint(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val lower = (ex.rootCause?.message ?: ex.message ?: "").lowercase()
        val status = if (listOf("duplicate", "unique", "constraint", "exclusion", "overlap").any { it in lower })
            HttpStatus.CONFLICT else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = ex.rootCause?.message ?: ex.message
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message
            )
        )
}

