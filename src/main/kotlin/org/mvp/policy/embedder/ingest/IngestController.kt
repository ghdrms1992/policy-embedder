package org.mvp.policy.embedder.ingest

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/ingest")
class IngestController(
    private val ingestService: IngestService
) {
    @PostMapping("/pptx", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun ingestPptx(
        @RequestPart("file") file: MultipartFile,
        @RequestParam title: String,
        @RequestParam(required = false, defaultValue = "") version: String
    ): Map<String, Any> {
        val count = file.inputStream.use { ingestService.ingestPptx(it, title, version, file.originalFilename ?: "policy.pptx") }
        return mapOf("status" to "ok", "chunks" to count)
    }
}