package com.template.contracts

import com.template.states.GameState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class GameContract : Contract {

    companion object {
        const val ID = "com.template.contracts.TemplateContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
    }

    interface Commands : CommandData {
        class StartGame : Commands
        class SubmitTurn : Commands
        class EndGame : Commands
    }

    class BoardUtils {
        companion object {

            fun checkIfValidStartBoard(board: Array<CharArray>): Boolean {
                return true
            }

            fun checkIfValidBoardUpdate(inputBoard: Array<CharArray>, outputBoard: Array<CharArray>, playerChar: Char): Boolean {
                return true
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
        val board: Array<CharArray> = Array(10, {charArrayOf('E', 'E', 'E', 'E', 'E', 'E','E', 'E', 'E', 'E')} )
    }
}