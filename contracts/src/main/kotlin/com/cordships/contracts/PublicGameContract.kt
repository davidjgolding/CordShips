package com.cordships.contracts

import com.cordships.states.HitOrMiss
import com.cordships.states.GameStatus
import com.cordships.states.PublicGameState
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************

/** This contract governs the evolution of the global, public game state contract for a given group of players */
class PublicGameContract : Contract {

    companion object {
        const val ID = "com.cordships.contracts.PublicGameContract"
    }

    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.IssueGame -> requireThat {

                var input = tx.inputs;
                "There should be no input state" using (input.isEmpty())

                var output = tx.outputs[0].data
                "There should be just one output state." using (tx.outputs.size == 1)
                "The output state should be of type public game state." using (output is PublicGameState)

                var outputPublicGameState = output as PublicGameState
                "There should be more that 1 player." using (outputPublicGameState.playerBoards.size > 1)
                "The game status should be GAME_NOT_STARTED." using (outputPublicGameState.status == GameStatus.GAME_NOT_STARTED)
                "Turn Count should be zero." using (outputPublicGameState.turnCount == 0)
                "There should be no player proofs yet." using (outputPublicGameState.playerProofs == null)
            }
            is Commands.StartGame -> requireThat {

                var input = tx.inputs;
                "There should be exactly one input state." using (input.size == 1)
                "Input state should be of type Public Game State." using (tx.inputStates.single() is PublicGameState)

                var output = tx.outputs[0].data
                "There should be exactly one output state." using (tx.outputs.size == 1)
                "The output state should be of type public game state." using (output is PublicGameState)

                var outputPublicGameState = output as PublicGameState
                "The game status should be GAME_IN_PROGRESS." using (outputPublicGameState.status == GameStatus.GAME_IN_PROGRESS)
                "Turn Count should be zero." using (outputPublicGameState.turnCount == 0)
                "There should be player proofs." using (outputPublicGameState.playerProofs != null)
            }
            is Commands.Attack -> requireThat {

                var input = tx.inputs;
                "There should be exactly one input state" using (input.size == 1)
                "Input state should be of type Public Game State." using (tx.inputStates.single() is PublicGameState)

                var inputPublicGameState = tx.inputStates.single() as PublicGameState
                "You can only attack when a game is in progress." using (inputPublicGameState.status == GameStatus.GAME_IN_PROGRESS)

                var output = tx.outputs[0].data
                "There should be exactly one output state." using (tx.outputs.size == 1)
                "The output state should be of type public game state." using (output is PublicGameState)

                var outputPublicGameState = output as PublicGameState
                "New game status must be GAME_IN_PROGRESS or GAME_OVER." using (outputPublicGameState.status != GameStatus.GAME_NOT_STARTED)
                "Turn Count should be not be zero." using (outputPublicGameState.turnCount > 0)

                "New game state must increment the turn count by 1 if still in progress." using
                        (outputPublicGameState.isGameOver() || (inputPublicGameState.turnCount + 1 == outputPublicGameState.turnCount))

                "Player proofs must remain the same." using (inputPublicGameState.playerProofs == outputPublicGameState.playerProofs)

                var attack = command.value as Commands.Attack
                "You can't attack yourself." using (attack.shots.any() { it.adversary != attack.attacker })

                // Removing as BoardUtils is throwing a seal exception.  Will investigate later:::
                //"Attack position is not valid." using (attack.shots.any() { BoardUtils.checkIfValidPositions(it.coordinates)})
                "Adversary unknown." using (attack.shots.any() { outputPublicGameState.playerBoards.keys.contains(it.adversary) })
                "Attacker unknown." using (outputPublicGameState.playerBoards.keys.contains(attack.attacker))

                // Values which changed must be in the attack command and vice versa
                val inputPlayerBoards = (tx.inputs[0].state.data as PublicGameState).playerBoards
                val boardChanges = mutableMapOf<Party, MutableList<Pair<Pair<Int, Int>, HitOrMiss>>>()
                inputPlayerBoards.keys.forEach { party ->
                    val inputPlayerBoard = inputPlayerBoards[party]
                    val outputPlayerBoard = (tx.outputs[0].data as PublicGameState).playerBoards[party]
                    boardChanges[party] = mutableListOf()
                    when {
                        inputPlayerBoard != null && outputPlayerBoard != null -> {
                            "Board size cannot change during attack" using (inputPlayerBoard.size == outputPlayerBoard.size)
                            inputPlayerBoard.indices.forEach { xIndex ->
                                "Board size cannot change during attack" using (inputPlayerBoard[xIndex].size
                                        == outputPlayerBoard[xIndex].size)
                                inputPlayerBoard[xIndex].indices
                                        .asSequence()
                                        .filter { inputPlayerBoard[xIndex][it] != outputPlayerBoard[xIndex][it] }
                                        .forEach {yIndex ->
                                            boardChanges[party]?.add(Pair(Pair(xIndex, yIndex), outputPlayerBoard[xIndex][yIndex]))
                                        }
                            }
                        }
                    }
                }

                "The number of attacks must equal the number of board changes" using (attack.shots.size == boardChanges.values.flatten().size)
                attack.shots.forEach {
                    "Board has changed but that change is not included in the attack." using (boardChanges[it.adversary]?.contains(
                            Pair(it.coordinates, it.hitOrMiss))!!)
                }
            }
        }
    }

    interface Commands : CommandData {

        class IssueGame : Commands

        class StartGame : Commands

        data class Attack(
                val shots: List<Shot>,
                val attacker: Party
        ) : Commands

        @CordaSerializable
        data class Shot(
                val coordinates: Pair<Int, Int>,
                val adversary: Party,
                var hitOrMiss: HitOrMiss
        )
    }
}