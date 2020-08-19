package com.cordships.states

import com.cordships.contracts.HitResponseContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(HitResponseContract::class)
data class HitResponseState(
        val attacker: Party,
        val owner: Party,
        val gameStateId: UniqueIdentifier,
        val turnCount: Int,
        val hitOrMiss: HitOrMiss,
        override val participants: List<AbstractParty> = listOf(owner),
        override val linearId: UniqueIdentifier = makeId(gameStateId, turnCount)
) : LinearState {
    companion object {
        fun makeId(gameStateId: UniqueIdentifier, turnCount: Int) =
                UniqueIdentifier("${gameStateId}_$turnCount")
    }
}