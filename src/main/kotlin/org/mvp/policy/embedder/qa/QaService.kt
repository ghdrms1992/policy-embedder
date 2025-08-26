package org.mvp.policy.embedder.qa

import org.springframework.stereotype.Service
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.document.Document

@Service
class QaService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    fun answer(q: String, k: Int, version: String?): Pair<String, List<Document>> {
        // 1) 벡터 검색
        val hits = vectorStore.similaritySearch(
            SearchRequest.builder().query(q).topK(k).build()
        )

        // 2) (선택) 버전 필터
        val filtered = version?.let { v ->
            hits.filter { it.metadata["version"]?.toString() == v }
        } ?: hits

        // 컨텍스트 없으면 즉시 응답
        if (filtered.isEmpty()) {
            return "해당 정책에서 확인되지 않습니다. (근거 없음)" to hits
        }

        // 3) 컨텍스트 구성
        val ctx = buildContext(filtered)

        // 4) LLM 호출
        val sys = SystemMessage(
            """
            당신은 '정책서 기반 QA' 어시스턴트입니다.
            - 제공된 컨텍스트 범위 내에서만 답하십시오.
            - 불명확하거나 근거가 없으면 '해당 정책에서 확인되지 않습니다'라고 답하십시오.
            - 답변 내에 출처를 언급하지 말고 마지막에 따로 분리해서 표기하십시오.
            """.trimIndent()
        )
        val user = UserMessage("질문: $q\n\n다음 컨텍스트만 사용:\n$ctx")

        val resp = chatModel.call(Prompt(listOf(sys, user)))
        val answer = resp.result.output.text ?: ""
        return answer to filtered
    }

    private fun buildContext(docs: List<Document>): String =
        docs.take(8).joinToString("\n\n") { d ->
            val m = d.metadata
            val head = "[${m["title"] ?: "제목없음"} v${m["version"] ?: "-"} / ${m["section"] ?: "-"}]"
            val body = (d.text ?: "").trim().take(1500)
            "$head\n$body"
        }.take(8000) // 전체 길이 가드
}
