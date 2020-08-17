package com.cordships.flows

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class NewGame(private val players: List<Party>) : FlowLogic<GameState>() {
    override fun call(): GameState {
        return GameState(listOf(ourIdentity) + players)
    }
}
