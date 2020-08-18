package com.cordships.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class HitQueryContract : Contract {
    companion object {
        const val ID = "com.cordships.contracts.HitQueryContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Issue : Commands
    }
}