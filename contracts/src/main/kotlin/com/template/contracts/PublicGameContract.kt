package com.cordships.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class PublicGameContract : Contract {

    companion object {
        const val ID = "com.cordships.contracts.publicgamecontract"
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
}