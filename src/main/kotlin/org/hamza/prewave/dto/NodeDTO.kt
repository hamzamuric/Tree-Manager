package org.hamza.prewave.dto

data class NodeDTO(
    val id: Int,
    val children: MutableList<NodeDTO>,
)

fun treeFromEdgeList(root: Int, edges: List<EdgeDTO>): NodeDTO {
    val groups = edges.groupBy { it.from }

    val tree = NodeDTO(id = root, children = mutableListOf())
    val q = ArrayDeque<NodeDTO>()
    q.addFirst(tree)

    while (q.isNotEmpty()) {
        val currentNode = q.removeFirst()
        val current = currentNode.id
        val children = groups[current] ?: continue
        for (e in children) {
            val child = NodeDTO(id = e.to!!, children = mutableListOf())
            currentNode.children.add(child)
            q.addLast(child)
        }
    }

    return tree
}
