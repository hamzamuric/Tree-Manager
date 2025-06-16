package org.hamza.prewave.service.abstraction

import org.hamza.prewave.dto.EdgeDTO

interface EdgeService {
    fun getEdges(root: Int): List<EdgeDTO>
    fun streamEdges(root: Int): Sequence<EdgeDTO>
    fun createEdge(dto: EdgeDTO): EdgeDTO
    fun deleteEdge(dto: EdgeDTO)
    fun deleteEdgeRecursive(dto: EdgeDTO)
}