package org.mvp.policy.embedder.ingest

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
class SearchController(
    private val ingestService: IngestService
) {
    data class Hit(
        val content: String,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val metadata: Map<String, Any>?
    )

    @GetMapping
    fun search(@RequestParam q: String, @RequestParam(defaultValue = "5") k: Int): List<Hit> =
        ingestService.searchPreview(q, k).map { Hit(it.text?:"", it.metadata) }
}
