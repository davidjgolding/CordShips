package com.template.contracts

import com.template.states.GameState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class GameContract : Contract {

    companion object {
        const val ID = "com.template.contracts.GameContract"
    }

    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands>()

        when(command.value) {

            is Commands.StartGame -> requireThat {

            }
            is Commands.SubmitTurn -> requireThat {

            }
            is Commands.EndGame -> requireThat {

            }
        }
    }

    interface Commands : CommandData {
        class StartGame : Commands
        class SubmitTurn : Commands
        class EndGame : Commands
    }

    class BoardUtils {
        companion object {

            fun checkIfValidPositions(positions: List<Pair<Int, Int>>) : Boolean {
                for(position in positions) {
                    if (!checkIfValidPosition(position))
                        return false
                }
                return true
            }

            private fun checkIfValidPosition(position: Pair<Int, Int>) : Boolean {
                if(position.first < 0 || position.first > 10) return false;
                if(position.second < 0 || position.second > 10) return false;
                return true;
            }

            fun isGameOver(gameState: GameState): Boolean {
                return false
            }

            fun getWinner(gameState: GameState): UniqueIdentifier? {
                return null
            }
        }
    }

    class Board {
        val board: List<List<Int>> = listOf(
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0),
                listOf(0,0,0,0,0,0,0,0,0,0)
        )
    }
}