package org.mvp.policy.embedder.document.controller

import org.mvp.policy.embedder.document.dto.IngestResponse
import org.mvp.policy.embedder.document.dto.SearchResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: org.mvp.policy.embedder.document.service.DocumentService
) {
    
    @PostMapping("/ingest/pptx", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun ingestPptx(
        @RequestPart("file") file: MultipartFile,
        @RequestParam docId: String,
        @RequestParam(required = false, defaultValue = "1.0") version: String,
        @RequestParam(required = false, defaultValue = "document") category: String
    ): IngestResponse {
        return file.inputStream.use { inputStream ->
            documentService.ingestPptx(
                input = inputStream,
                docId = docId,
                version = version,
                filename = file.originalFilename ?: "document.pptx",
                category = category,
                fileSize = file.size
            )
        }
    }
    
    @GetMapping("/search")
    fun search(
        @RequestParam q: String, 
        @RequestParam(defaultValue = "5") k: Int
    ): SearchResponse {
        return documentService.searchDocuments(q, k)
    }
}