package org.hamza.prewave.exception

data class ApiErrors(
    val status: Int,
    val error: String,
    val messages: List<String>,
    val path: String,
)
