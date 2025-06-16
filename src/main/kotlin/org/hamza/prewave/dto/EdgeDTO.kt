package org.hamza.prewave.dto

import com.example.jooq.generated.tables.records.EdgeRecord
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Min

data class EdgeDTO(
    @JsonProperty(required = true)
    @field:Min(0, message = "'from' value must be at least 0")
    val from: Int,

    @JsonProperty(required = true)
    @field:Min(0, message = "'from' value must be at least 0")
    val to: Int,
)

fun EdgeRecord.toDto() = EdgeDTO(from = fromId, to = toId)