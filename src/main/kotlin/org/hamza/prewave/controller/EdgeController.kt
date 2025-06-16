package org.hamza.prewave.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import org.hamza.prewave.controller.docs.EdgeApi
import org.hamza.prewave.dto.EdgeDTO
import org.hamza.prewave.dto.NodeDTO
import org.hamza.prewave.dto.treeFromEdgeList
import org.hamza.prewave.service.abstraction.EdgeService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.BufferedOutputStream

@RestController
@RequestMapping("/edge")
class EdgeController(
    private val edgeService: EdgeService,
    private val objectMapper: ObjectMapper,
) : EdgeApi {

    @GetMapping("/{root}", produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun getEdges(@PathVariable root: Int): ResponseEntity<List<EdgeDTO>> =
        ResponseEntity.ok(edgeService.getEdges(root))

    @GetMapping("/{root}/pretty", produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun getEdgesPretty(@PathVariable root: Int): ResponseEntity<NodeDTO> =
        ResponseEntity.ok(
            edgeService.getEdges(root).let { edges ->
                treeFromEdgeList(root, edges)
            }
        )

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun createEdge(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<EdgeDTO> =
        ResponseEntity(edgeService.createEdge(dto), HttpStatus.CREATED)

    @DeleteMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun deleteEdge(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<Any> {
        edgeService.deleteEdge(dto)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/recursive", produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun deleteEdgeRecursive(@Valid @RequestBody dto: EdgeDTO): ResponseEntity<Any> {
        edgeService.deleteEdgeRecursive(dto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{root}/stream", produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun getEdgesStream(@PathVariable root: Int): ResponseEntity<StreamingResponseBody> {
        val batchSize = 2 // change to 500

        val stream = StreamingResponseBody { outputStream ->
            val out = BufferedOutputStream(outputStream)

            out.write("[".toByteArray())

            edgeService.streamEdges(root).forEachIndexed { index, edge ->
                if (index > 0) out.write(",".toByteArray())
                val json = objectMapper.writeValueAsString(edge)
                out.write(json.toByteArray())
                if ((index + 1) % batchSize == 0) out.flush()
            }

            out.write("]".toByteArray())
            out.flush()
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(stream)
    }
}