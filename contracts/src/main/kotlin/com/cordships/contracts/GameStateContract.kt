package com.cordships.contracts

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class GameStateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.cordships.contracts.GameStateContract"
    }
    override fun verify(tx: LedgerTransaction) {

    }
}
