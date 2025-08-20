package org.mvp.policy.embedder.qa

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ask")
class AskController(
    private val qaService: QaService
) {
    data class AskReq(val q: String, val k: Int = 6, val version: String? = null)
    data class Source(val title: String?, val version: String?, val section: String?)
    data class AskRes(val answer: String, val sources: List<Source>)

    @PostMapping
    fun ask(@RequestBody req: AskReq): AskRes {
        val (answer, docs) = qaService.answer(req.q, req.k, req.version)
        return AskRes(
            answer = answer,
            sources = docs.map { d ->
                val m = d.metadata
                Source(
                    title = m["title"]?.toString(),
                    version = m["version"]?.toString(),
                    section = m["section"]?.toString()
                )
            }
        )
    }
}
