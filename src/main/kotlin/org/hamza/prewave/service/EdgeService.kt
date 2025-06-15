package org.hamza.prewave.service

import com.example.jooq.generated.tables.Edge.EDGE
import org.hamza.prewave.dto.EdgeDTO
import org.hamza.prewave.dto.toDto
import org.hamza.prewave.exception.ResourceNotFoundException
import org.jooq.CommonTableExpression
import org.jooq.DSLContext
import org.jooq.Record2
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.INTEGER
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class EdgeService(private val dsl: DSLContext) {
    fun getEdges(root: Int): List<EdgeDTO> {
        val cte = getTreeCte(root)

        return dsl.withRecursive(cte)
            .selectFrom(cte)
            .fetch()
            .map { it.into(EDGE).toDto() }
    }

    fun streamEdges(root: Int): Sequence<EdgeDTO> {
        val cte = getTreeCte(root)

        return dsl.withRecursive(cte)
            .selectFrom(cte)
            .fetchLazy()
            .asSequence()
            .map { it.into(EDGE).toDto() }
    }

    fun createEdge(dto: EdgeDTO): EdgeDTO {
        val record = dsl.newRecord(EDGE).apply {
            fromId = dto.from
            toId = dto.to
        }

        try {
            record.store()
        } catch (e: DuplicateKeyException) {
            throw DuplicateKeyException("Edge from ${dto.from} to ${dto.to} already exists", e)
        }
        return record.toDto()
    }

    fun deleteEdge(dto: EdgeDTO) {
        val deleted = dsl.deleteFrom(EDGE)
            .where(EDGE.FROM_ID.eq(dto.from).and(EDGE.TO_ID.eq(dto.to)))
            .execute()

        if (deleted == 0) throw ResourceNotFoundException("Edge from ${dto.from} to ${dto.to} does not exist")
    }

    fun deleteEdgeRecursive(dto: EdgeDTO) {
        dsl.transaction { trx ->
            val deleted = trx.dsl().deleteFrom(EDGE)
                .where(EDGE.FROM_ID.eq(dto.from).and(EDGE.TO_ID.eq(dto.to)))
                .execute()

            val cte = getTreeCte(dto.to!!)
            trx.dsl().deleteFrom(EDGE)
                .where(
                    field("(from_id, to_id)").`in`(
                        trx.dsl().withRecursive(cte)
                            .selectFrom(cte)
                    )
                )
                .execute()

            if (deleted == 0) throw ResourceNotFoundException("Edge from ${dto.from} to ${dto.to} does not exist")
        }
    }

    private fun getTreeCte(root: Int): CommonTableExpression<Record2<Int?, Int?>> {
        val TREE = "tree"
        return name(TREE).fields("from_id", "to_id").`as`(
            select(EDGE.FROM_ID, EDGE.TO_ID)
                .from(EDGE)
                .where(EDGE.FROM_ID.eq(root))
                .union(
                    select(EDGE.FROM_ID, EDGE.TO_ID)
                        .from(EDGE)
                        .innerJoin(table(name(TREE)))
                        .on(EDGE.FROM_ID.eq(field(name(TREE, "to_id"), INTEGER)))

                )
        )
    }
}