package org.hamza.prewave.dto

import com.example.jooq.generated.tables.records.EdgeRecord
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class EdgeDTO(
    @field:NotNull(message = "'from' field is required")
    @field:Min(0, message = "'from' value must be at least 0")
    val from: Int?,

    @field:NotNull(message = "'to' field is required")
    @field:Min(0, message = "'from' value must be at least 0")
    val to: Int?,
)

fun EdgeRecord.toDto() = EdgeDTO(from = fromId, to = toId)