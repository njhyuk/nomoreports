package com.njhyuk.nomoreports.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CommitReportRequest(
    val author: String,
    val repository: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val since: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val until: LocalDate? = null,
    val host: String = "https://api.github.com",
    val token: String? = null,
    val reportType: String? = "commit" // "commit" 또는 "pr"
) 