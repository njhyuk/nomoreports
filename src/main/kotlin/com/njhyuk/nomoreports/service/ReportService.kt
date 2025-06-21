package com.njhyuk.nomoreports.service

import com.njhyuk.nomoreports.model.ClassifiedCommit
import com.njhyuk.nomoreports.model.CommitType
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ReportService {

    fun generateMarkdownReport(
        commits: List<ClassifiedCommit>,
        author: String,
        repository: String,
        since: String,
        until: String?
    ): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        val report = StringBuilder()
        report.append("# NoMoreReports - 커밋 리포트\n\n")
        report.append("## 📊 요약 정보\n\n")
        report.append("- **저장소**: `$repository`\n")
        report.append("- **작성자**: `$author`\n")
        report.append("- **기간**: `$since` ~ `${until ?: "현재"}`\n")
        report.append("- **총 커밋 수**: `${commits.size}`\n\n")

        // 타입별 통계
        val typeStats = commits.groupBy { it.type }
        report.append("## 📈 타입별 통계\n\n")
        CommitType.values().forEach { type ->
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
            CommitType.values().forEach { type ->
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
} 