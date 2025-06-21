package com.njhyuk.nomoreports.model

import java.time.OffsetDateTime

data class ClassifiedCommit(
    val sha: String,
    val message: String,
    val date: OffsetDateTime,
    val type: CommitType
) 