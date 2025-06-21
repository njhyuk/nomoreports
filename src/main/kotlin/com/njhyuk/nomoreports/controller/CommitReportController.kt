package com.njhyuk.nomoreports.controller

import com.njhyuk.nomoreports.model.ClassifiedCommit
import com.njhyuk.nomoreports.model.CommitReportRequest
import com.njhyuk.nomoreports.model.GitHubCommit
import com.njhyuk.nomoreports.service.GitHubService
import com.njhyuk.nomoreports.service.OllamaService
import com.njhyuk.nomoreports.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api")
class CommitReportController(
    private val gitHubService: GitHubService,
    private val ollamaService: OllamaService,
    private val reportService: ReportService
) {

    @PostMapping("/commit-report")
    fun generateCommitReport(@RequestBody request: CommitReportRequest): ResponseEntity<Map<String, String>> {
        return try {
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

            // 3. 마크다운 리포트 생성
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val sinceStr = request.since.format(formatter)
            val untilStr = request.until?.format(formatter)
            
            val markdownReport = reportService.generateMarkdownReport(
                commits = classifiedCommits,
                author = request.author,
                repository = request.repository ?: "모든 저장소",
                since = sinceStr,
                until = untilStr
            )

            ResponseEntity.ok(mapOf("report" to markdownReport))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }
} 