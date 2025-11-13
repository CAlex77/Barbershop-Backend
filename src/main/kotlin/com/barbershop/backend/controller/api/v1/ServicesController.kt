package com.barbershop.backend.controller.api.v1

import com.barbershop.backend.dto.request.ServiceRequest
import com.barbershop.backend.dto.response.PagedResponse
import com.barbershop.backend.dto.response.ServiceResponse
import com.barbershop.backend.service.ImageStorageService
import com.barbershop.backend.service.ServiceService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class ServicesController(
    private val serviceService: ServiceService,
    private val imageStorageService: ImageStorageService
) {

    @GetMapping("/services", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) dir: String?,
        @RequestParam(required = false) active: Boolean?,
        @RequestParam(required = false) category: String?
    ): PagedResponse<ServiceResponse> = serviceService.list(page, size, sort, dir, active, category)

    @GetMapping("/services/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: Long): ResponseEntity<ServiceResponse> =
        serviceService.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PostMapping("/services", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody @Valid req: ServiceRequest): ResponseEntity<ServiceResponse> {
        val saved = serviceService.create(req)
        return ResponseEntity.created(URI.create("/api/v1/services/${saved.serviceId}"))
            .body(saved)
    }

    @PutMapping("/services/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@PathVariable id: Long, @RequestBody @Valid req: ServiceRequest): ResponseEntity<ServiceResponse> =
        serviceService.update(id, req)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/services/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (serviceService.delete(id)) ResponseEntity.noContent().build() else ResponseEntity.notFound().build()

    // Image upload and fetch
    @PostMapping("/services/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun uploadImage(@PathVariable id: Long, @RequestPart("file") file: MultipartFile): ResponseEntity<ServiceResponse> =
        serviceService.uploadImage(id, file)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @GetMapping("/services/{id}/image")
    fun getImage(@PathVariable id: Long, request: HttpServletRequest): ResponseEntity<org.springframework.core.io.FileSystemResource> {
        val pathId = serviceService.getImagePath(id) ?: return ResponseEntity.notFound().build()
        val stored = imageStorageService.load(pathId)
        val ifNoneMatch = request.getHeader("If-None-Match")
        if (ifNoneMatch != null && ifNoneMatch == "\"${stored.etag}\"") {
            return imageStorageService.notModified()
        }
        return imageStorageService.toResponseEntity(stored)
    }
}
