package org.hamza.prewave.exception

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)