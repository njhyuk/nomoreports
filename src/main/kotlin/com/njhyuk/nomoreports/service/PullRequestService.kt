package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.PullRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class PullRequestService(
    private val webClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getPullRequests(
        owner: String,
        repo: String,
        since: LocalDate,
        token: String?,
        host: String = "https://api.github.com"
    ): List<PullRequest> {
        val apiHost = when {
            host.contains("api.github.com") -> host.trimEnd('/')
            host.contains("/api/") -> host.trimEnd('/')
            else -> host.trimEnd('/') + "/api/v3"
        }
        val url = "$apiHost/repos/$owner/$repo/pulls"
        val sinceStr = since.format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        logger.info("Fetching PRs for $owner/$repo since $sinceStr from $apiHost")

        return try {
            val builder = webClient.get()
                .uri(url) { uriBuilder ->
                    uriBuilder
                        .queryParam("state", "all")
                        .queryParam("sort", "updated")
                        .queryParam("direction", "desc")
                        .queryParam("per_page", "100")
                        .build()
                }
                .header("Accept", "application/vnd.github.v3+json")

            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "token $token")
            }

            val response = builder
                .retrieve()
                .bodyToMono(Array<PullRequest>::class.java)
                .block()

            val prs = response?.toList() ?: emptyList()
            
            // since 날짜 이후의 PR만 필터링
            val filteredPrs = prs.filter { pr ->
                val prDate = LocalDate.parse(pr.updatedAt.substring(0, 10))
                prDate.isAfter(since) || prDate.isEqual(since)
            }

            logger.info("Found ${filteredPrs.size} PRs since $sinceStr")
            filteredPrs

        } catch (e: Exception) {
            logger.error("Failed to fetch PRs for $owner/$repo", e)
            emptyList()
        }
    }

    fun getPullRequestTitles(prs: List<PullRequest>): List<String> {
        return prs.map { it.title }
    }
} 