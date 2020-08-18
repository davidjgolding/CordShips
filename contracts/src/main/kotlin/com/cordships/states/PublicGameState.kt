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
 * @param playerBoards All players currently engaged in the game
 * @param status A flag indicating the state of the game
 * @param turnCount The number of turns that has passed
 * @param linearId A unique id for the game
 */
@BelongsToContract(PublicGameContract::class)
@CordaSerializable
data class PublicGameState constructor(val playerBoards: Map<Party, Board>,
                                       val playerProofs: Map<Party, Int>?,
                                       val status: GameStatus,
                                       val turnCount: Int,
                                       override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

    /**
     * A public constructor for initiating a new game with only players provided as parties
     *
     * @param players The players provided that will be participating in this game
     */
    constructor(players: Set<Party>): this(
        players.map { it to Array(10) { Array(10) { HitOrMiss.UNKNOWN } }}.toMap(),
        null,
        GameStatus.GAME_NOT_STARTED,
        0
    )

    /** All of the players are participants to this game board */
    override val participants: List<AbstractParty> = playerBoards.keys.toList()

    /** Starts the game */
    fun startGame(playerProofs: Map<Party, Int>) = copy(playerProofs = playerProofs, status = GameStatus.GAME_IN_PROGRESS)

    /** Returns the party representing the player who's turn it is currently */
    fun getCurrentPlayerParty(): AbstractParty { return playerBoards.keys.toList()[turnCount % playerBoards.size]}

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
        val newPlayerBoard = playerBoards[playerToAttack]
            ?.map { board -> board.map { row -> row }.toTypedArray() }
            ?.toTypedArray()
            ?: throw IllegalArgumentException("Player: $playerToAttack does not have a board")
        newPlayerBoard[attackCoordinates.first][attackCoordinates.second] = hitOrMiss

        // Update the map of player boards
        val mutablePlayerBoards = playerBoards.toMutableMap()
        mutablePlayerBoards[playerToAttack] = newPlayerBoard
        return copy(playerBoards = mutablePlayerBoards)
    }
}

/** Represents the outcome of an attach */
@CordaSerializable
enum class HitOrMiss {
    HIT,
    MISS,
    UNKNOWN
}

/** A simple enum for representing game status */
@CordaSerializable
enum class GameStatus {
    GAME_NOT_STARTED, GAME_IN_PROGRESS, GAME_OVER
}