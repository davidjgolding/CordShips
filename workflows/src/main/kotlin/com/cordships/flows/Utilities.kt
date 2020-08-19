package com.cordships.flows

import com.cordships.states.HitResponseState
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria

/** Simple utility for getting the default notary */
fun ServiceHub.defaultNotary() = this.networkMapCache.notaryIdentities.first()

fun ServiceHub.loadPublicGameState(gameStateId: UniqueIdentifier): StateAndRef<PublicGameState> =
        vaultService.queryBy<PublicGameState>(
                QueryCriteria.LinearStateQueryCriteria(linearId = listOf(gameStateId))
        ).states.single()

fun ServiceHub.loadPrivateGameState(gameStateId: UniqueIdentifier): StateAndRef<PrivateGameState> =
        vaultService.queryBy<PrivateGameState>()
                .states.single { it.state.data.associatedPublicGameState == gameStateId }

fun ServiceHub.loadHitResponseState(gameStateId: UniqueIdentifier, owner: Party, turnCount: Int): StateAndRef<HitResponseState>? {
        val id = HitResponseState.makeId(gameStateId, owner, turnCount)
        return vaultService.queryBy<HitResponseState>().states.singleOrNull {
                it.state.data.uniqueId == id
        }
}
