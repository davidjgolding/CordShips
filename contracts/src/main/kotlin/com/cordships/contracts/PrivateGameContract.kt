package com.cordships.contracts

import com.cordships.states.PrivateGameState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************

/** This contract governs the state evolution of private copies of game states for each player. */
class PrivateGameContract : Contract {
    companion object {
        const val ID = "com.cordships.contracts.PrivateGameContract"
    }

    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands>()

        when(command.value) {

            is Commands.IssuePrivateGameState -> requireThat{

                "There should be no input state." using (tx.inputs.isEmpty())
                "There should be one output state." using (tx.outputs.size == 1)
                "The output state should be of type private game state." using (tx.outputs[0].data is PrivateGameState)
            }
        }
    }

    interface Commands : CommandData {
        class IssuePrivateGameState : Commands
    }
}