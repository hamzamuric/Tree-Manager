package org.hamza.prewave.service.impl

import com.example.jooq.generated.tables.Edge
import com.example.jooq.generated.tables.Edge.EDGE
import org.hamza.prewave.dto.EdgeDTO
import org.hamza.prewave.dto.toDto
import org.hamza.prewave.exception.CycleDetectedException
import org.hamza.prewave.exception.InvalidTreeException
import org.hamza.prewave.exception.ResourceNotFoundException
import org.hamza.prewave.service.abstraction.EdgeService
import org.jooq.CommonTableExpression
import org.jooq.DSLContext
import org.jooq.Record2
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

@Service
class EdgeServiceImpl(private val dsl: DSLContext) : EdgeService {
    override fun getEdges(root: Int): List<EdgeDTO> {
        val cte = getTreeCte(root)

        return dsl.withRecursive(cte)
            .selectFrom(cte)
            .fetch()
            .map { it.into(Edge.EDGE).toDto() }
    }

    override fun streamEdges(root: Int): Sequence<EdgeDTO> {
        val cte = getTreeCte(root)

        return dsl.withRecursive(cte)
            .selectFrom(cte)
            .fetchLazy()
            .asSequence()
            .map { it.into(EDGE).toDto() }
    }

    override fun createEdge(dto: EdgeDTO): EdgeDTO {
        if (dto.from == dto.to) throw CycleDetectedException()

        // check for potential cycle
        val cycleCheck = dsl.selectDistinct(EDGE.FROM_ID)
            .from(EDGE)
            .where(EDGE.FROM_ID.eq(dto.to))
            .union(
                dsl.selectDistinct(EDGE.TO_ID)
                    .from(EDGE)
                    .where(EDGE.TO_ID.eq(dto.from))
            )
            .fetch()

        if (cycleCheck.count() == 2) throw CycleDetectedException()

        // check for an invalid tree
        val invalidTreeCheck = dsl.selectFrom(EDGE)
            .where(EDGE.TO_ID.eq(dto.to))
            .fetchOne()

        if (invalidTreeCheck != null) throw InvalidTreeException()

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

    override fun deleteEdge(dto: EdgeDTO) {
        val deleted = dsl.deleteFrom(EDGE)
            .where(EDGE.FROM_ID.eq(dto.from).and(EDGE.TO_ID.eq(dto.to)))
            .execute()

        if (deleted == 0) throw ResourceNotFoundException("Edge from ${dto.from} to ${dto.to} does not exist")
    }

    override fun deleteEdgeRecursive(dto: EdgeDTO) {
        dsl.transaction { trx ->
            val deleted = trx.dsl().deleteFrom(EDGE)
                .where(EDGE.FROM_ID.eq(dto.from).and(EDGE.TO_ID.eq(dto.to)))
                .execute()

            if (deleted == 0) throw ResourceNotFoundException("Edge from ${dto.from} to ${dto.to} does not exist")

            val cte = getTreeCte(dto.to)
            trx.dsl().deleteFrom(EDGE)
                .where(
                    DSL.field("(from_id, to_id)").`in`(
                        trx.dsl().withRecursive(cte)
                            .selectFrom(cte)
                    )
                )
                .execute()
        }
    }

    private fun getTreeCte(root: Int): CommonTableExpression<Record2<Int?, Int?>> {
        val TREE = "tree"
        return DSL.name(TREE).fields("from_id", "to_id").`as`(
            DSL.select(EDGE.FROM_ID, EDGE.TO_ID)
                .from(EDGE)
                .where(EDGE.FROM_ID.eq(root))
                .union(
                    DSL.select(EDGE.FROM_ID, EDGE.TO_ID)
                        .from(EDGE)
                        .innerJoin(DSL.table(DSL.name(TREE)))
                        .on(EDGE.FROM_ID.eq(DSL.field(DSL.name(TREE, "to_id"), SQLDataType.INTEGER)))

                )
        )
    }
}