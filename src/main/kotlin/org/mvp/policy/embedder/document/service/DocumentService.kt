package org.mvp.policy.embedder.document.service

import org.mvp.policy.embedder.document.dto.IngestResponse
import org.mvp.policy.embedder.document.dto.SearchResponse
import org.mvp.policy.embedder.document.dto.SearchHit
import org.mvp.policy.embedder.document.dto.DocumentMetadata
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class DocumentService(
    private val vectorStore: VectorStore
) {
    private val pptxLoader = org.mvp.policy.embedder.document.util.PptxLoader()
    
    fun ingestPptx(input: InputStream, docId: String, version: String, filename: String, category: String = "document", fileSize: Long? = null): IngestResponse {
        return try {
            val docs = pptxLoader.load(input, docId, version, filename, category, fileSize)
            
            if (docs.isEmpty()) {
                return IngestResponse("error", 0, "문서를 읽을 수 없습니다")
            }
            
            val splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(50)
                .build()
            
            val chunks = splitter.apply(docs)
            vectorStore.add(chunks)
            
            IngestResponse("success", chunks.size, "성공적으로 처리되었습니다")
            
        } catch (e: Exception) {
            IngestResponse("error", 0, "처리 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    fun searchDocuments(query: String, topK: Int = 5): SearchResponse {
        return try {
            val searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build()
            
            val results = vectorStore.similaritySearch(searchRequest)
            
            val hits = results.map { doc ->
                SearchHit(
                    content = doc.text ?: "",
                    metadata = DocumentMetadata(
                        docId = doc.metadata["docId"]?.toString(),
                        version = doc.metadata["version"]?.toString(),
                        category = doc.metadata["category"]?.toString(),
                        fileSize = doc.metadata["fileSize"]?.toString()?.toLongOrNull(),
                        filename = doc.metadata["filename"]?.toString(),
                        uploadTime = doc.metadata["uploadTime"]?.toString(),
                        contentType = doc.metadata["contentType"]?.toString(),
                        lastUpdated = doc.metadata["lastUpdated"]?.toString()?.toLongOrNull(),
                        pageNumber = doc.metadata["page_number"]?.toString()?.toIntOrNull()
                    )
                )
            }
            
            SearchResponse(hits, hits.size)
            
        } catch (e: Exception) {
            SearchResponse(emptyList(), 0)
        }
    }
}