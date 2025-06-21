package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.GitHubCommit
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
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

    fun getCommits(
        repository: String?,
        author: String,
        since: LocalDate,
        until: LocalDate?,
        host: String = defaultHost,
        token: String? = defaultToken
    ): List<GitHubCommit> {
        return if (repository != null && repository.isNotBlank()) {
            getCommitsFromRepository(repository, author, since, until, host, token)
        } else {
            getCommitsFromUserEvents(author, since, until, host, token)
        }
    }

    private fun getCommitsFromRepository(
        repository: String,
        author: String,
        since: LocalDate,
        until: LocalDate?,
        host: String,
        token: String?
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
                .bodyToMono(List::class.java)
                .block() as List<GitHubCommit>
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch commits from GitHub: ${e.message}", e)
        }
    }

    private fun getCommitsFromUserEvents(
        author: String,
        since: LocalDate,
        until: LocalDate?,
        host: String,
        token: String?
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

        val uriBuilder = StringBuilder("/users/$author/events")
            .append("?per_page=100")
        
        if (untilStr != null) {
            uriBuilder.append("&until=${untilStr}T23:59:59Z")
        }

        return try {
            val events: List<Map<String, Any>> = client.get()
                .uri(uriBuilder.toString())
                .retrieve()
                .bodyToMono(List::class.java)
                .block() as List<Map<String, Any>>

            // PushEvent만 필터링하고 커밋 정보 추출
            events.filter { event ->
                event["type"] == "PushEvent" && 
                isEventInDateRange(event["created_at"]?.toString(), since, until)
            }.flatMap { event ->
                val payload = event["payload"] as? Map<String, Any>
                val commits = payload?.get("commits") as? List<Map<String, Any>> ?: emptyList()
                
                commits.map { commit ->
                    val commitSha = commit["sha"] as? String ?: ""
                    val commitMessage = commit["message"] as? String ?: ""
                    val commitAuthor = commit["author"] as? Map<String, Any>
                    val authorName = commitAuthor?.get("name") as? String ?: author
                    val authorEmail = commitAuthor?.get("email") as? String ?: ""
                    val eventDate = event["created_at"] as? String ?: ""
                    
                    GitHubCommit(
                        sha = commitSha,
                        commit = com.njhyuk.nomoreports.model.Commit(
                            message = commitMessage,
                            author = com.njhyuk.nomoreports.model.CommitAuthor(
                                name = authorName,
                                email = authorEmail,
                                date = if (eventDate.isNotEmpty()) {
                                    java.time.OffsetDateTime.parse(eventDate)
                                } else {
                                    java.time.OffsetDateTime.now()
                                }
                            ),
                            committer = com.njhyuk.nomoreports.model.CommitAuthor(
                                name = authorName,
                                email = authorEmail,
                                date = if (eventDate.isNotEmpty()) {
                                    java.time.OffsetDateTime.parse(eventDate)
                                } else {
                                    java.time.OffsetDateTime.now()
                                }
                            )
                        ),
                        author = null,
                        committer = null
                    )
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch user events from GitHub: ${e.message}", e)
        }
    }

    private fun isEventInDateRange(eventDateStr: String?, since: LocalDate, until: LocalDate?): Boolean {
        if (eventDateStr == null) return false
        
        return try {
            val eventDate = java.time.OffsetDateTime.parse(eventDateStr).toLocalDate()
            val isAfterSince = !eventDate.isBefore(since)
            val isBeforeUntil = until == null || !eventDate.isAfter(until)
            
            isAfterSince && isBeforeUntil
        } catch (e: Exception) {
            false
        }
    }
} 