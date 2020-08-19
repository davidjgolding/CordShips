package com.cordships.flows

import com.cordships.states.HitQueryState
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
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

fun ServiceHub.loadHitQueryState(gameStateId: UniqueIdentifier, turnCount: Int): StateAndRef<HitQueryState>? =
        vaultService.queryBy<HitQueryState>(
                QueryCriteria.LinearStateQueryCriteria(linearId = listOf(HitQueryState.makeId(gameStateId, turnCount)))
        ).states.singleOrNull()
