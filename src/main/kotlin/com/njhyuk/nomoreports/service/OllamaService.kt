package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.CommitType
import com.njhyuk.nomoreports.model.OllamaRequest
import com.njhyuk.nomoreports.model.OllamaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class OllamaService(
    private val webClient: WebClient
) {
    
    @Value("\${app.ollama.base-url:http://localhost:11434}")
    private lateinit var baseUrl: String
    
    @Value("\${app.ollama.model:llama2}")
    private lateinit var model: String

    fun classifyCommitsBatch(commitMessages: List<String>): List<CommitType> {
        if (commitMessages.isEmpty()) return emptyList()
        
        val prompt = buildString {
            appendLine("다음 커밋 메시지들을 분석하여 각각의 타입을 선택해주세요.")
            appendLine("가능한 타입: FEATURE, FIX, REFACTOR, TEST, DOCS, CHORE")
            appendLine()
            appendLine("커밋 메시지들:")
            commitMessages.forEachIndexed { index, message ->
                appendLine("${index + 1}. $message")
            }
            appendLine()
            appendLine("응답 형식: 각 줄에 타입만 작성해주세요 (예: FEATURE)")
        }

        val request = OllamaRequest(
            model = model,
            prompt = prompt
        )

        return try {
            val response = webClient.post()
                .uri("$baseUrl/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponse::class.java)
                .block()

            val responseText = response?.response?.trim() ?: ""
            val lines = responseText.split("\n").filter { it.isNotBlank() }
            
            lines.map { line ->
                val typeStr = line.trim().uppercase()
                CommitType.entries.find { it.name == typeStr } ?: CommitType.CHORE
            }.let { types ->
                // 응답 개수가 커밋 개수와 다르면 기본값으로 채움
                if (types.size < commitMessages.size) {
                    types + List(commitMessages.size - types.size) { CommitType.CHORE }
                } else {
                    types.take(commitMessages.size)
                }
            }
        } catch (e: Exception) {
            // 오류 발생 시 모든 커밋을 CHORE로 분류
            List(commitMessages.size) { CommitType.CHORE }
        }
    }
} 