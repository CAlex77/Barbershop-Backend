package com.barbershop.backend.service

import org.springframework.core.io.FileSystemResource
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class ImageStorageService(
    rootDir: String = "uploads"
) {
    private val root: Path = Path.of(rootDir).toAbsolutePath().normalize().apply { Files.createDirectories(this) }
    private val maxSize = DataSize.ofMegabytes(5).toBytes()

    data class StoredImage(
        val id: String,
        val path: Path,
        val contentType: String,
        val size: Long,
        val etag: String,
        val lastModified: Instant
    )

    fun save(file: MultipartFile): StoredImage {
        require(!file.isEmpty) { "Arquivo vazio" }
        require(file.size <= maxSize) { "Arquivo maior que o limite" }
        val ct = (file.contentType ?: "application/octet-stream").lowercase()
        require(ct.startsWith("image/")) { "Apenas imagens são aceitas" }

        val id = UUID.randomUUID().toString()
        val target = root.resolve(id)
        file.inputStream.use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
        val size = Files.size(target)
        val lastMod = Files.getLastModifiedTime(target).toMillis()
        val etag = hexSha256("$id:$size:$lastMod")
        return StoredImage(id, target, ct, size, etag, Instant.ofEpochMilli(lastMod))
    }

    fun load(id: String): StoredImage {
        val target = root.resolve(id)
        require(Files.exists(target)) { "Imagem não encontrada" }
        val ct = Files.probeContentType(target) ?: "application/octet-stream"
        val size = Files.size(target)
        val lastMod = Files.getLastModifiedTime(target).toMillis()
        val etag = hexSha256("$id:$size:$lastMod")
        return StoredImage(id, target, ct, size, etag, Instant.ofEpochMilli(lastMod))
    }

    fun fileUrlFor(id: String) = "/api/v1/images/$id"

    fun toResponseEntity(img: StoredImage): ResponseEntity<FileSystemResource> =
        ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(img.contentType))
            .contentLength(img.size)
            .eTag("\"${img.etag}\"")
            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
            .body(FileSystemResource(img.path))

    fun notModified(): ResponseEntity<FileSystemResource> =
        ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()

    private fun hexSha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

