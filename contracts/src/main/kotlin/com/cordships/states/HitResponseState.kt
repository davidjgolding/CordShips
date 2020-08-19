package com.cordships.states

import com.cordships.contracts.HitResponseContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@BelongsToContract(HitResponseContract::class)
data class HitResponseState(
        val attacker: Party,
        val owner: Party,
        val gameStateId: UniqueIdentifier,
        val turnCount: Int,
        val hitOrMiss: HitOrMiss,
        override val participants: List<AbstractParty>,
        val uniqueId: String = makeId(gameStateId, owner, turnCount)
) : QueryableState {
    companion object {
        fun makeId(gameStateId: UniqueIdentifier, owner: Party, turnCount: Int) =
                "${gameStateId}_${turnCount}_${owner.name}"
    }
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is HitResponseV1 -> HitResponseV1.PersistentHitResponse(
                    this.uniqueId.toString(),
                    this.attacker.name.toString(),
                    this.owner.name.toString(),
                    this.turnCount,
                    this.hitOrMiss.toString()
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(HitResponseV1)
}

object HitResponse

object HitResponseV1 : MappedSchema(
        schemaFamily = HitResponse.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentHitResponse::class.java)) {
    @Entity
    @Table(name = "hit_response_states")
    class PersistentHitResponse(
            @Column(name = "uniqueId", unique = true)
            var uniqueId: String,

            @Column(name = "attacker")
            var attacker: String,

            @Column(name = "owner")
            var owner: String,

            @Column(name = "turnCount")
            var turnCount: Int,

            @Column(name = "hitOrMiss")
            var hitOrMiss: String
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "",0, "")
    }
}