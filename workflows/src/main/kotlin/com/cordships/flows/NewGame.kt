package com.cordships.flows

import com.cordships.Board
import com.cordships.states.GameStatus
import com.cordships.states.PublicGameState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party

@StartableByRPC
class NewGame(private val players: List<Party>) : FlowLogic<PublicGameState>() {
    override fun call(): PublicGameState {
        return PublicGameState(
            listOf(ourIdentity) + players,
            listOf(),
            GameStatus.GAME_IN_PROGRESS,
            0
        )
    }
}
