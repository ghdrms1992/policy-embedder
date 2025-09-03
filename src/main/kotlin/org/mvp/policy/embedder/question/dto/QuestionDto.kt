package org.mvp.policy.embedder.question.dto

data class AskRequest(
    val q: String,
    val k: Int = 6,
    val version: String? = null
)

data class AskResponse(
    val answer: String,
    val sources: List<Source>
)

data class Source(
    val docId: String?,
    val version: String?,
    val category: String?,
    val filename: String?,
    val pageNumber: Int?
)