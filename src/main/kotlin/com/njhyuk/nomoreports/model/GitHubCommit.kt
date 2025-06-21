package com.njhyuk.nomoreports.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class GitHubCommit(
    val sha: String,
    val commit: Commit,
    val author: Author?,
    val committer: Author?
)

data class Commit(
    val message: String,
    val author: CommitAuthor,
    val committer: CommitAuthor
)

data class CommitAuthor(
    val name: String,
    val email: String,
    val date: OffsetDateTime
)

data class Author(
    val login: String,
    val id: Long,
    @JsonProperty("avatar_url")
    val avatarUrl: String,
    val type: String
) 