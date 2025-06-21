package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.ClassifiedCommit
import com.njhyuk.nomoreports.model.CommitType
import com.njhyuk.nomoreports.model.PullRequest
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ReportService {
    fun generateMarkdownReportWithSummary(
        commits: List<ClassifiedCommit>,
        author: String,
        repository: String,
        since: String,
        until: String?,
        summary: String
    ): String {
        val report = StringBuilder()
        report.append("# NoMoreReports - 커밋 리포트\n\n")
        report.append("## 🏆 성과 요약\n\n")
        report.append(summary.trim()).append("\n\n")
        report.append("## 📊 요약 정보\n\n")
        report.append("- **저장소**: `$repository`\n")
        report.append("- **작성자**: `$author`\n")
        report.append("- **기간**: `$since` ~ `${until ?: "현재"}`\n")
        report.append("- **총 커밋 수**: `${commits.size}`\n\n")

        // 타입별 통계
        val typeStats = commits.groupBy { it.type }
        report.append("## 📈 타입별 통계\n\n")
        CommitType.entries.forEach { type ->
            val count = typeStats[type]?.size ?: 0
            val percentage = if (commits.isNotEmpty()) {
                String.format("%.1f", (count.toDouble() / commits.size) * 100)
            } else "0.0"
            report.append("- ${type.emoji} **${type.label}**: ${count}개 ($percentage%)\n")
        }
        report.append("\n")

        // 날짜별 커밋
        val dateGroups = commits.groupBy {
            it.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }.toSortedMap(compareByDescending { it })

        report.append("## 📅 날짜별 커밋\n\n")
        dateGroups.forEach { (date, dayCommits) ->
            report.append("### $date (${dayCommits.size}개)\n\n")
            val typeGroups = dayCommits.groupBy { it.type }
            CommitType.entries.forEach { type ->
                val typeCommits = typeGroups[type] ?: emptyList()
                if (typeCommits.isNotEmpty()) {
                    report.append("#### ${type.emoji} ${type.label}\n\n")
                    typeCommits.forEach { commit ->
                        val time = commit.date.format(DateTimeFormatter.ofPattern("HH:mm"))
                        report.append("- **$time** `${commit.sha.take(7)}` ${commit.message}\n")
                    }
                    report.append("\n")
                }
            }
        }
        return report.toString()
    }

    fun generatePullRequestReportWithSummary(
        pullRequests: List<PullRequest>,
        author: String,
        repository: String,
        since: String,
        until: String?,
        summary: String
    ): String {
        val report = StringBuilder()
        report.append("# NoMoreReports - PR 리포트\n\n")
        report.append("## 🏆 성과 요약\n\n")
        report.append(summary.trim()).append("\n\n")
        report.append("## 📊 요약 정보\n\n")
        report.append("- **저장소**: `$repository`\n")
        report.append("- **작성자**: `$author`\n")
        report.append("- **기간**: `$since` ~ `${until ?: "현재"}`\n")
        report.append("- **총 PR 수**: `${pullRequests.size}`\n\n")

        // 상태별 통계
        val stateStats = pullRequests.groupBy { it.state }
        report.append("## 📈 상태별 통계\n\n")
        val states = listOf("open", "closed")
        states.forEach { state ->
            val count = stateStats[state]?.size ?: 0
            val percentage = if (pullRequests.isNotEmpty()) {
                String.format("%.1f", (count.toDouble() / pullRequests.size) * 100)
            } else "0.0"
            val emoji = when (state) {
                "open" -> "🟢"
                "closed" -> "🔴"
                else -> "⚪"
            }
            val label = when (state) {
                "open" -> "열린 PR"
                "closed" -> "닫힌 PR"
                else -> state
            }
            report.append("- $emoji **$label**: ${count}개 ($percentage%)\n")
        }
        report.append("\n")

        // 날짜별 PR
        val dateGroups = pullRequests.groupBy {
            it.updatedAt.substring(0, 10) // YYYY-MM-DD
        }.toSortedMap(compareByDescending { it })

        report.append("## 📅 날짜별 PR\n\n")
        dateGroups.forEach { (date, dayPRs) ->
            report.append("### $date (${dayPRs.size}개)\n\n")
            val stateGroups = dayPRs.groupBy { it.state }
            states.forEach { state ->
                val statePRs = stateGroups[state] ?: emptyList()
                if (statePRs.isNotEmpty()) {
                    val emoji = when (state) {
                        "open" -> "🟢"
                        "closed" -> "🔴"
                        else -> "⚪"
                    }
                    val label = when (state) {
                        "open" -> "열린 PR"
                        "closed" -> "닫힌 PR"
                        else -> state
                    }
                    report.append("#### $emoji $label\n\n")
                    statePRs.forEach { pr ->
                        val time = pr.updatedAt.substring(11, 16) // HH:mm
                        val merged = if (pr.mergedAt != null) " (병합됨)" else ""
                        report.append("- **$time** `#$${pr.number}` ${pr.title}$merged\n")
                    }
                    report.append("\n")
                }
            }
        }
        return report.toString()
    }
} 