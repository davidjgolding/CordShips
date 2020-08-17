package com.cordships.states

import com.cordships.Board
import com.cordships.BoardUtils
import com.cordships.contracts.PublicGameContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.lang.IllegalArgumentException

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
data class PublicGameState(val players: Map<Party, Board>,
                           val status: GameStatus = GameStatus.GAME_IN_PROGRESS,
                           val turnCount: Int,
                           override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    /** All of the players are participants to this game board */
    override val participants: List<AbstractParty> = players.keys.toList()

    /** Returns the party representing the player who's turn it is currently */
    fun getCurrentPlayerParty(): AbstractParty { return players.keys.toList()[turnCount % players.size]}

    /** Returns a copy of the GameBoard with the turn count incremented */
    fun endTurn(): PublicGameState = copy(turnCount = turnCount + 1)

    /** Returns a copy of a BoardState object after an attack at Pair<x,y> */
    fun updateBoardWithAttack(
            attackCoordinates: Pair<Int,Int>,
            playerToAttack: Party,
            hitOrMiss: HitOrMiss
    ): PublicGameState {

        // Check if the coordinates of the attack are valid
        if (!BoardUtils.checkIfValidPositions(attackCoordinates))
            throw IllegalStateException("Invalid board index.")

        // Retrieve, copy and mutate the board of the player being attacked
        val newPlayerBoard = players[playerToAttack]
            ?.map { board -> board.map { row -> row }.toTypedArray() }
            ?.toTypedArray()
            ?: throw IllegalArgumentException("Player: $playerToAttack does not have a board")
        newPlayerBoard[attackCoordinates.first][attackCoordinates.second] = hitOrMiss

        // Update the map of player boards
        val mutablePlayerBoards = players.toMutableMap()
        mutablePlayerBoards[playerToAttack] = newPlayerBoard
        return copy(players = mutablePlayerBoards)
    }
}

/** Represents the outcome of an attach */
enum class HitOrMiss {
    HIT,
    MISS
}

/** A simple enum for representing game status */
@CordaSerializable
enum class GameStatus {
    GAME_IN_PROGRESS, GAME_OVER
}