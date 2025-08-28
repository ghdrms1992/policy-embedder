package org.mvp.policy.embedder.qa

import org.springframework.stereotype.Service
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.document.Document

@Service
class QaService(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient
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

        // 4) LLM 호출 (ChatClient API 사용)
        val answer = chatClient.prompt()
            .user("질문: $q\n\n다음 컨텍스트만 사용:\n$ctx")
            .call()
            .content() ?: ""
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
