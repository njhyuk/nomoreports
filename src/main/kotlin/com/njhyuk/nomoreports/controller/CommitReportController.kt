package com.njhyuk.nomoreports.controller

import com.njhyuk.nomoreports.model.ClassifiedCommit
import com.njhyuk.nomoreports.model.CommitReportRequest
import com.njhyuk.nomoreports.model.GitHubCommit
import com.njhyuk.nomoreports.model.PullRequest
import com.njhyuk.nomoreports.service.GitHubService
import com.njhyuk.nomoreports.service.OllamaService
import com.njhyuk.nomoreports.service.PullRequestService
import com.njhyuk.nomoreports.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api")
class CommitReportController(
    private val gitHubService: GitHubService,
    private val ollamaService: OllamaService,
    private val pullRequestService: PullRequestService,
    private val reportService: ReportService
) {

    @PostMapping("/commit-report")
    fun generateCommitReport(@RequestBody request: CommitReportRequest): ResponseEntity<Map<String, String>> {
        try {
            // 1. GitHub에서 커밋 데이터 가져오기
            val commits = gitHubService.getCommits(
                repository = request.repository,
                author = request.author,
                since = request.since,
                until = request.until,
                host = request.host,
                token = request.token
            )

            // 2. 모든 커밋을 한 번에 LLM으로 분류
            val commitMessages = commits.map { it.commit.message }
            val commitTypes = ollamaService.classifyCommitsBatch(commitMessages)
            val classifiedCommits = commits.zip(commitTypes).map { (commit, type) ->
                ClassifiedCommit(
                    sha = commit.sha,
                    message = commit.commit.message,
                    date = commit.commit.author.date,
                    type = type
                )
            }

            // 2-1. 성과 요약 생성
            val summary = ollamaService.summarizeCommits(commitMessages)

            // 3. 마크다운 리포트 생성
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val sinceStr = request.since.format(formatter)
            val untilStr = request.until?.format(formatter)
            val markdownReport = reportService.generateMarkdownReportWithSummary(
                commits = classifiedCommits,
                author = request.author,
                repository = request.repository ?: "모든 저장소",
                since = sinceStr,
                until = untilStr,
                summary = summary
            )

            return ResponseEntity.ok(mapOf("report" to markdownReport))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @PostMapping("/pr-report")
    fun generatePullRequestReport(@RequestBody request: CommitReportRequest): ResponseEntity<Map<String, String>> {
        try {
            if (request.repository.isNullOrBlank()) {
                return ResponseEntity.badRequest().body(mapOf("error" to "PR 리포트를 생성하려면 저장소가 필요합니다."))
            }

            // 저장소 이름에서 owner와 repo 추출
            val parts = request.repository.split("/")
            if (parts.size != 2) {
                return ResponseEntity.badRequest().body(mapOf("error" to "저장소 형식이 올바르지 않습니다. (예: owner/repo)"))
            }
            val owner = parts[0]
            val repo = parts[1]

            // 1. GitHub에서 PR 데이터 가져오기
            val pullRequests = pullRequestService.getPullRequests(
                owner = owner,
                repo = repo,
                since = request.since,
                token = request.token,
                host = request.host
            )

            // 2. PR 제목으로 성과 요약 생성
            val prTitles = pullRequestService.getPullRequestTitles(pullRequests)
            val summary = ollamaService.summarizeCommits(prTitles) // 기존 함수 재사용

            // 3. 마크다운 리포트 생성
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val sinceStr = request.since.format(formatter)
            val untilStr = request.until?.format(formatter)
            val markdownReport = reportService.generatePullRequestReportWithSummary(
                pullRequests = pullRequests,
                author = request.author,
                repository = request.repository,
                since = sinceStr,
                until = untilStr,
                summary = summary
            )

            return ResponseEntity.ok(mapOf("report" to markdownReport))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @PostMapping("/report")
    fun generateReport(@RequestBody request: CommitReportRequest): ResponseEntity<Map<String, String>> {
        try {
            return when (request.reportType?.lowercase()) {
                "pr" -> generatePullRequestReport(request)
                "commit", null -> generateCommitReport(request)
                else -> ResponseEntity.badRequest().body(mapOf("error" to "지원하지 않는 리포트 타입입니다. (commit 또는 pr)"))
            }
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }
} 