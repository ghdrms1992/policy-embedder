package org.mvp.policy.policy.embedder.ingest

import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class IngestService(
    private val vectorStore: VectorStore,
) {
    private val loader = PptxLoader()

    fun ingestPptx(input: InputStream, title: String, version: String, filename: String): Int {
        // 1) PPTX 로드 → 문서 목록
        val docs: List<Document> = loader.load(input, title, version, filename)
        if (docs.isEmpty()) return 0

        // 2) 토큰 기준 청킹 (Spring AI 제공)
        val splitter = TokenTextSplitter.builder()
            .withChunkSize(800)              // 목표 토큰 크기
            .withMinChunkSizeChars(350)      // 자연스런 분리점 확보
            .withMinChunkLengthToEmbed(50)
            .build()
        val splitDocs = splitter.apply(docs)

        // 3) 벡터스토어 적재 (임베딩은 EmbeddingModel 빈으로 자동 계산)
        vectorStore.add(splitDocs)
        return splitDocs.size
    }

    fun searchPreview(query: String, topK: Int = 5) =
        vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(topK).build()
        )
}