package org.hamza.prewave.controller.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hamza.prewave.dto.EdgeDTO
import org.hamza.prewave.dto.NodeDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@Tag(name = "Edge", description = "Edge API")
interface EdgeApi {

    @Operation(
        summary = "Get edge list",
        description = "List of all the edges rooted in the given node"
    )
    @ApiResponse(responseCode = "200", description = "Edges returned successfully")
    fun getEdges(root: Int): ResponseEntity<List<EdgeDTO>>

    @Operation(
        summary = "Get edges pretty",
        description = "Get edges in format of nested node object forming a tree rooted in the given node"
    )
    @ApiResponse(responseCode = "200", description = "Edges returned successfully")
    fun getEdgesPretty(@PathVariable root: Int): ResponseEntity<NodeDTO>

    @Operation(
        summary = "Create an edge",
        description = "Creates a new edge"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Edge created successfully"),
        ApiResponse(responseCode = "409", description = "Edge would create a cycle"),
        ApiResponse(responseCode = "409", description = "Edge would create an invalid tree"),
        ApiResponse(responseCode = "409", description = "Edge already exists"),
    ])
    fun createEdge(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<EdgeDTO>

    @Operation(
        summary = "Delete an edge",
        description = "Deletes edge"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Edge deleted successfully"),
        ApiResponse(responseCode = "404", description = "Edge not found"),
    ])
    fun deleteEdge(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<Any>

    @Operation(
        summary = "Delete edges recursively",
        description = "Deletes a given edge and all of the edges from the subtree rooted in the edge"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Edge deleted successfully"),
        ApiResponse(responseCode = "404", description = "Edge not found"),
    ])
    fun deleteEdgeRecursive(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<Any>

    @Operation(
        summary = "Get streaming list of edges",
        description = "Stream a list of edges rooted in the given node"
    )
    fun getEdgesStream(@PathVariable root: Int): ResponseEntity<StreamingResponseBody>
}