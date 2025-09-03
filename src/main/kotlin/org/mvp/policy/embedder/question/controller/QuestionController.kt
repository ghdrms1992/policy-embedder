package org.mvp.policy.embedder.question.controller

import org.mvp.policy.embedder.question.dto.AskRequest
import org.mvp.policy.embedder.question.dto.AskResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")
class QuestionController(
    private val questionService: org.mvp.policy.embedder.question.service.QuestionService
) {
    
    @PostMapping("/ask")
    fun ask(@RequestBody request: AskRequest): AskResponse {
        return questionService.askQuestion(
            question = request.q,
            topK = request.k,
            version = request.version
        )
    }
}