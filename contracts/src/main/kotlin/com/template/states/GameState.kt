package com.cordships.states

import com.cordships.Board
import com.cordships.BoardUtils
import com.cordships.contracts.GameContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable


// *********
// * State *
// *********
@CordaSerializable
enum class GameStatus {
    GAME_IN_PROGRESS, GAME_OVER
}

@BelongsToContract(GameContract::class)
@CordaSerializable
data class GameState(val players: List<AbstractParty>,
                     val boards: List<Board>,
                     val currentPlayer: Party,
                     val status: GameStatus = GameStatus.GAME_IN_PROGRESS,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<AbstractParty> = players

    private fun Array<CharArray>.copy() = Array(size) { get(it).clone() }

    // Returns the party of the current player
    fun getCurrentPlayerParty(): Party { return currentPlayer }

    // Returns a copy of a BoardState object after a move at Pair<x,y>
    fun returnNewBoardAfterMove(pos: List<Pair<Int,Int>>, boardBeingAttacked:Board): GameState {

        // Check if the index is valid
        if (false) throw IllegalStateException("Invalid board index.")

        // Update who's turn it is
        // Create a new game state
        val newGameState = this

        if (BoardUtils.isGameOver(newGameState))
            return newGameState.copy(status = GameStatus.GAME_OVER)

        return newGameState
    }
}