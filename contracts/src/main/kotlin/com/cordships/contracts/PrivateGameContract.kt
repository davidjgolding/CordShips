package com.cordships.contracts

import com.cordships.states.PrivateGameState
import com.cordships.states.Ship
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

                var input = tx.inputs;
                "There should be no input state." using (input.isEmpty())

                var output = tx.outputs[0].data
                "There should be one output state." using (tx.outputs.size == 1)
                "The output state should be of type private game state." using (tx.outputs[0].data is PrivateGameState)

                var outputPrivateGameState = output as PrivateGameState
                "There should be 7 ships." using (outputPrivateGameState.board.size == 7)
                "There needs to be exactly one airCraftCarrier." using (outputPrivateGameState.board.count {it.shipSize == Ship.ShipSize.AirCraftCarrier} == 1)
                "There needs to be exactly one battleship." using (outputPrivateGameState.board.count {it.shipSize == Ship.ShipSize.Cruiser} == 1)
                "There needs to be exactly one cruiser." using (outputPrivateGameState.board.count {it.shipSize == Ship.ShipSize.BattleShip} == 1)
                "There needs to be exactly two Destroyers." using (outputPrivateGameState.board.count {it.shipSize == Ship.ShipSize.Destroyer} == 2)
                "There needs to be exactly two Submarines." using (outputPrivateGameState.board.count {it.shipSize == Ship.ShipSize.Submarine} == 2)
                "AirCraftCarrier needs to be of size 5." using (Ship.ShipSize.AirCraftCarrier.length == 5)
                "BattleShip needs to be of size 4." using (Ship.ShipSize.BattleShip.length == 4)
                "Cruiser needs to be of size 3." using (Ship.ShipSize.Cruiser.length == 3)
                "Destroyer needs to be of size 2." using (Ship.ShipSize.Destroyer.length == 2)
                "Submarine needs to be of size 2." using (Ship.ShipSize.Submarine.length == 2)

                var requiredSigners = outputPrivateGameState.participants.map { it.owningKey }
                "Owner must sign the transaction." using (command.signers == requiredSigners)
            }
        }
    }

    interface Commands : CommandData {
        class IssuePrivateGameState : Commands
    }
}