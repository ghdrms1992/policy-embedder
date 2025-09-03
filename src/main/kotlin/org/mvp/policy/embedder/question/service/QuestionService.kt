package org.mvp.policy.embedder.question.service

import org.mvp.policy.embedder.question.dto.AskResponse
import org.mvp.policy.embedder.question.dto.Source
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.document.Document
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient
) {
    
    fun askQuestion(question: String, topK: Int, version: String?): AskResponse {
        // 1. 벡터 검색
        val searchRequest = SearchRequest.builder()
            .query(question)
            .topK(topK)
            .build()
        
        val hits = vectorStore.similaritySearch(searchRequest)
        
        // 2. 버전 필터링 (옵션)
        val filteredHits = version?.let { v ->
            hits.filter { it.metadata["version"]?.toString() == v }
        } ?: hits
        
        // 3. 컨텍스트 없으면 기본 응답
        if (filteredHits.isEmpty()) {
            return AskResponse(
                answer = "해당 정책에서 확인되지 않습니다. (근거 없음)",
                sources = emptyList()
            )
        }
        
        // 4. LLM 호출
        val context = buildContext(filteredHits)
        val answer = chatClient.prompt()
            .user("질문: $question\n\n다음 컨텍스트만 사용:\n$context")
            .call()
            .content() ?: "답변을 생성할 수 없습니다."
        
        // 5. 소스 정보 구성
        val sources = filteredHits.map { doc ->
            Source(
                docId = doc.metadata["docId"]?.toString(),
                version = doc.metadata["version"]?.toString(),
                category = doc.metadata["category"]?.toString(),
                filename = doc.metadata["filename"]?.toString(),
                pageNumber = doc.metadata["page_number"]?.toString()?.toIntOrNull()
            )
        }
        
        return AskResponse(answer, sources)
    }
    
    private fun buildContext(docs: List<Document>): String =
        docs.take(8).joinToString("\n\n") { doc ->
            val metadata = doc.metadata
            val header = "[${metadata["docId"] ?: "문서ID없음"} v${metadata["version"] ?: "-"} / Page ${metadata["page_number"] ?: "-"}]"
            val content = (doc.text ?: "").trim().take(1500)
            "$header\n$content"
        }.take(8000)
}