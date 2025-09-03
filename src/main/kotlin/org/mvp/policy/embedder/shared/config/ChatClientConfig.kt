package org.mvp.policy.embedder.shared.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultSystem(
                """
                당신은 '정책서 기반 QA' 어시스턴트입니다.
                - 제공된 컨텍스트 범위 내에서만 답하십시오.
                - 불명확하거나 근거가 없으면 '해당 정책에서 확인되지 않습니다'라고 답하십시오.
                - 답변 내에 출처를 언급하지 말고 마지막에 따로 분리해서 표기하십시오.
                """.trimIndent()
            )
            .build()
    }
}