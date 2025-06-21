package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.GitHubCommit
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class GitHubService(
    private val webClient: WebClient
) {
    
    @Value("\${app.github.default-token:}")
    private lateinit var defaultToken: String
    
    @Value("\${app.github.default-host:https://api.github.com}")
    private lateinit var defaultHost: String

    suspend fun getCommits(
        repository: String,
        author: String,
        since: LocalDate,
        until: LocalDate?,
        host: String = defaultHost,
        token: String? = defaultToken
    ): List<GitHubCommit> {
        // GitHub.com의 경우 기본 URL 사용, GHE의 경우 /api/v3 추가
        val apiUrl = if (host.contains("github.com") && !host.contains("/api/v3")) {
            host
        } else if (host.contains("/api/v3")) {
            host
        } else {
            "$host/api/v3"
        }
        
        val client = if (token != null && token.isNotBlank()) {
            webClient.mutate()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "token $token")
                .build()
        } else {
            webClient.mutate()
                .baseUrl(apiUrl)
                .build()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sinceStr = since.format(formatter)
        val untilStr = until?.format(formatter)

        val uriBuilder = StringBuilder("/repos/$repository/commits")
            .append("?author=$author")
            .append("&since=${sinceStr}T00:00:00Z")
        
        if (untilStr != null) {
            uriBuilder.append("&until=${untilStr}T23:59:59Z")
        }

        return try {
            client.get()
                .uri(uriBuilder.toString())
                .retrieve()
                .awaitBody()
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch commits from GitHub: ${e.message}", e)
        }
    }
} 