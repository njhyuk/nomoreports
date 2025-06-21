package com.njhyuk.nomoreports.model

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

data class OllamaResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean
) 