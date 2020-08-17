package com.cordships.contracts

import net.corda.core.contracts.*
import net.corda.core.identity.Party
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
        data class Attack(
                val x: Int,
                val y: Int,
                val attacker: Party,
                val adversary: Party,
                var isHit: Boolean
        ): Commands
    }
}