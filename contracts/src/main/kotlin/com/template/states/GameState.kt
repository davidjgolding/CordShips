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
                     val status: GameStatus = GameStatus.GAME_IN_PROGRESS,
                     val turnCount: Int,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<AbstractParty> = players

    // Returns the party of the current player
    fun getCurrentPlayerParty(): AbstractParty { return players[turnCount % players.size]}

    // Returns a copy of a BoardState object after a move at Pair<x,y>
    fun returnNewBoardAfterMove(pos: List<Pair<Int,Int>>, boardBeingAttacked:Board): GameState {

        // Check if the index is valid
        if (!BoardUtils.checkIfValidPositions(pos))
            throw IllegalStateException("Invalid board index.")

        // Create a new game state
        val newGameState = this.copy(turnCount = turnCount + 1)

        if (BoardUtils.isGameOver(newGameState))
            return newGameState.copy(status = GameStatus.GAME_OVER)

        return newGameState
    }
}