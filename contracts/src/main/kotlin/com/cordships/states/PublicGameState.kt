package com.cordships.states

import com.cordships.Board
import com.cordships.BoardUtils
import com.cordships.contracts.PublicGameContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

/**
 * The global, public state of a given game of CordShips. This state indicated all public information
 * including attempted attacks (hits / misses) for each respective players public game board. It also
 * encapsulates turn logic and will be used to advance play at the end of each players turn.
 *
 * @param players All players currently engaged in the game
 * @param boards A list of all public game boards
 * @param status A flag indicating the state of the game
 * @param turnCount The number of turns that has passed
 * @param linearId A unique id for the game
 */
@BelongsToContract(PublicGameContract::class)
@CordaSerializable
data class PublicGameState(val players: List<AbstractParty>,
                           val boards: List<Board>,
                           val status: GameStatus = GameStatus.GAME_IN_PROGRESS,
                           val turnCount: Int,
                           override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    override val participants: List<AbstractParty> = players

    // Returns the party of the current player
    fun getCurrentPlayerParty(): AbstractParty { return players[turnCount % players.size]}

    // Returns a copy of a BoardState object after a move at Pair<x,y>
    fun returnNewBoardAfterMove(pos: List<Pair<Int,Int>>, boardBeingAttacked:Board): PublicGameState {

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

/** A simple enum for representing game status */
@CordaSerializable
enum class GameStatus {
    GAME_IN_PROGRESS, GAME_OVER
}