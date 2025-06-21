package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.CommitType
import com.njhyuk.nomoreports.model.OllamaRequest
import com.njhyuk.nomoreports.model.OllamaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class OllamaService(
    private val webClient: WebClient
) {
    
    @Value("\${app.ollama.base-url:http://localhost:11434}")
    private lateinit var baseUrl: String
    
    @Value("\${app.ollama.model:tinyllama}")
    private lateinit var model: String

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun classifyCommitsBatch(commitMessages: List<String>): List<CommitType> {
        if (commitMessages.isEmpty()) return emptyList()
        
        val prompt = buildString {
            appendLine("커밋 타입 분류:")
            appendLine("FEATURE, FIX, REFACTOR, TEST, DOCS, CHORE")
            appendLine()
            commitMessages.forEachIndexed { index, message ->
                appendLine("${index + 1}. $message")
            }
            appendLine()
            appendLine("답변:")
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

            extractTypesFromResponse(responseText, commitMessages.size)
        } catch (e: Exception) {
            // 실패 시 기본값으로 CHORE 반환
            List(commitMessages.size) { CommitType.CHORE }
        }
    }

    private fun extractTypesFromResponse(response: String, expectedCount: Int): List<CommitType> {
        val lines = response.split("\n").filter { it.isNotBlank() }
        
        // 1. 각 줄에서 타입 추출 시도
        val extractedTypes = mutableListOf<CommitType>()
        
        for (line in lines) {
            val cleanLine = line.trim().removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("4.").removePrefix("5.").trim()
            
            when {
                cleanLine.uppercase().contains("FEATURE") -> extractedTypes.add(CommitType.FEATURE)
                cleanLine.uppercase().contains("FIX") -> extractedTypes.add(CommitType.FIX)
                cleanLine.uppercase().contains("REFACTOR") -> extractedTypes.add(CommitType.REFACTOR)
                cleanLine.uppercase().contains("TEST") -> extractedTypes.add(CommitType.TEST)
                cleanLine.uppercase().contains("DOCS") -> extractedTypes.add(CommitType.DOCS)
                cleanLine.uppercase().contains("CHORE") -> extractedTypes.add(CommitType.CHORE)
            }
        }
        
        // 2. 쉼표로 구분된 응답 처리
        if (extractedTypes.isEmpty()) {
            val commaSeparated = response.split(",").map { it.trim() }
            for (item in commaSeparated) {
                when {
                    item.uppercase().contains("FEATURE") -> extractedTypes.add(CommitType.FEATURE)
                    item.uppercase().contains("FIX") -> extractedTypes.add(CommitType.FIX)
                    item.uppercase().contains("REFACTOR") -> extractedTypes.add(CommitType.REFACTOR)
                    item.uppercase().contains("TEST") -> extractedTypes.add(CommitType.TEST)
                    item.uppercase().contains("DOCS") -> extractedTypes.add(CommitType.DOCS)
                    item.uppercase().contains("CHORE") -> extractedTypes.add(CommitType.CHORE)
                }
            }
        }
        
        // 3. 응답이 부족하면 CHORE로 채움
        while (extractedTypes.size < expectedCount) {
            extractedTypes.add(CommitType.CHORE)
        }
        
        // 4. 응답이 많으면 잘라냄
        return extractedTypes.take(expectedCount)
    }

    fun summarizeCommits(commitMessages: List<String>): String {
        if (commitMessages.isEmpty()) return "성과 요약을 생성할 커밋이 없습니다."

        val prompt = buildString {
            appendLine("다음은 최근 몇 달간의 커밋 메시지입니다.")
            appendLine("이 커밋들을 바탕으로 개발자의 주요 성과와 개선점을 3~5줄로 요약해주세요.")
            appendLine("반드시 한글로만 답변해주세요. 영어 사용을 금지합니다.")
            appendLine()
            commitMessages.forEachIndexed { index, message ->
                appendLine("${index + 1}. $message")
            }
        }

        logger.debug("=== OLLAMA SUMMARY PROMPT ===")
        logger.debug(prompt)
        logger.debug("=============================")

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

            val responseText = response?.response?.trim() ?: "요약 생성 실패"
            logger.debug("=== OLLAMA SUMMARY RESPONSE ===")
            logger.debug(responseText)
            logger.debug("==============================")
            responseText
        } catch (e: Exception) {
            logger.error("Ollama 성과 요약 API 호출 실패", e)
            "요약 생성 실패"
        }
    }
} 