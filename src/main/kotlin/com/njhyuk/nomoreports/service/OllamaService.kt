package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.CommitType
import com.njhyuk.nomoreports.model.OllamaRequest
import com.njhyuk.nomoreports.model.OllamaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class OllamaService(
    private val webClient: WebClient
) {
    
    @Value("\${app.ollama.base-url:http://localhost:11434}")
    private lateinit var baseUrl: String
    
    @Value("\${app.ollama.model:llama2}")
    private lateinit var model: String

    suspend fun classifyCommit(message: String): CommitType {
        val prompt = """
            다음 커밋 메시지를 분석하여 가장 적절한 타입을 선택해주세요.
            가능한 타입: FEATURE, FIX, REFACTOR, TEST, DOCS, CHORE
            
            커밋 메시지: $message
            
            타입만 응답해주세요 (예: FEATURE):
        """.trimIndent()

        val request = OllamaRequest(
            model = model,
            prompt = prompt
        )

        return try {
            val response = webClient.post()
                .uri("$baseUrl/api/generate")
                .bodyValue(request)
                .retrieve()
                .awaitBody<OllamaResponse>()

            val typeStr = response.response.trim().uppercase()
            CommitType.values().find { it.name == typeStr } ?: CommitType.CHORE
        } catch (e: Exception) {
            // 오류 발생 시 기본값으로 CHORE 반환
            CommitType.CHORE
        }
    }
} 