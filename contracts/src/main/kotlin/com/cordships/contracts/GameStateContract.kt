package com.cordships.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

class GameStateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.cordships.contracts.GameStateContract"
    }

    override fun verify(tx: LedgerTransaction) {

    }

    interface Commands : CommandData {
        class New : Commands
        data class Attack(
                val x: Int,
                val y: Int,
                val attacker: Party,
                val adversary: Party,
                var isHit: Boolean
        ) : Commands
    }
}
