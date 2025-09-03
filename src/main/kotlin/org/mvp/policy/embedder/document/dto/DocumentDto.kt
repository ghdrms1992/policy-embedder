package org.mvp.policy.embedder.document.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class IngestResponse(
    val status: String,
    val chunks: Int,
    val message: String? = null
)

data class SearchHit(
    val content: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val metadata: DocumentMetadata?
)

data class SearchResponse(
    val hits: List<SearchHit>,
    val totalCount: Int
)

data class DocumentMetadata(
    val docId: String?,
    val version: String?,
    val category: String?,
    val fileSize: Long?,
    val filename: String?,
    val uploadTime: String?,
    val contentType: String?,
    val lastUpdated: Long?,
    val pageNumber: Int?
)