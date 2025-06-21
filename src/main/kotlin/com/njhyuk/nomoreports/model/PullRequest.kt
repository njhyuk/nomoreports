package com.njhyuk.nomoreports.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val state: String,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    @JsonProperty("closed_at")
    val closedAt: String?,
    @JsonProperty("merged_at")
    val mergedAt: String?,
    val user: User,
    val body: String?
)

data class User(
    val login: String,
    val id: Long
) 