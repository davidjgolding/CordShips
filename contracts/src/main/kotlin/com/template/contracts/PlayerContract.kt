package com.template.contracts

import com.template.states.PlayerState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class PlayerContract : Contract {
    companion object {
        const val ID = "com.template.contracts.PlayerContract"
    }

    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands>()

        when(command.value) {

            is Commands.AddBattleShips -> requireThat{

                "There should be no input state." using (tx.inputs.isEmpty())
                "There should be one output state." using (tx.outputs.size == 1)
                "The output state should be of type BoardState." using (tx.outputs[0].data is PlayerState)

                val outputPlayerState = tx.outputStates[0] as PlayerState
                "The battle ship placement is not valid." using (GameContract.BoardUtils.checkIfValidBattleShipPlacement(outputPlayerState.battleShips))
            }
        }
    }

    interface Commands : CommandData {
        class AddBattleShips : Commands
    }
}