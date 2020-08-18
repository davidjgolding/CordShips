package com.cordships.contracts

import com.cordships.states.HitOrMiss
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

        when(command.value) {
            is Commands.IssueGame -> requireThat {

            }
            is Commands.StartGame -> requireThat {

            }
            is Commands.SubmitTurn -> requireThat {

            }
            is Commands.EndGame -> requireThat {

            }
        }
    }

    interface Commands : CommandData {

        class IssueGame: Commands

        class StartGame : Commands

        class SubmitTurn : Commands

        class EndGame : Commands

        data class Attack(
                val shots: List<Shot>,
                val attacker: Party
        ): Commands

        @CordaSerializable
        data class Shot(
                val coordinates: Pair<Int,Int>,
                val adversary: Party,
                var hitOrMiss: HitOrMiss
        )
    }
}