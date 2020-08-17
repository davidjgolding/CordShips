package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PrivateGameContract
import com.cordships.states.PrivateGameState
import com.cordships.states.Ship
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/** Places all pieces on a gameboard. */
@InitiatingFlow
@StartableByRPC
class PiecePlacement(private val airCraftCarrier: String, private val battleship: String,
                     private val cruiser: String, private val destroyer1: String,
                     private val destroyer2: String, private val submarine1: String,
                     private val submarine2: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Get already existing gameboard
        val criteria = QueryCriteria.VaultQueryCriteria(exactParticipants = listOf(ourIdentity))
        val playerStatesFromVault = serviceHub.vaultService.queryBy<PrivateGameState>(criteria).states

        val notary = if (playerStatesFromVault.isEmpty()) {
            serviceHub.networkMapCache.notaryIdentities.first()
        } else {
            playerStatesFromVault[0].state.notary
        }

        // Get points/coordinates for all inputs
        val coordinates = mutableListOf<Pair<Int, Int>>()
        coordinates.addAll(Ship(airCraftCarrier, Ship.ShipSize.AirCraftCarrier).coordinates)
        coordinates.addAll(Ship(battleship, Ship.ShipSize.BattleShip).coordinates)
        coordinates.addAll(Ship(cruiser, Ship.ShipSize.Cruiser).coordinates)
        coordinates.addAll(Ship(destroyer1, Ship.ShipSize.Destroyer).coordinates)
        coordinates.addAll(Ship(destroyer2, Ship.ShipSize.Destroyer).coordinates)
        coordinates.addAll(Ship(submarine1, Ship.ShipSize.Submarine).coordinates)
        coordinates.addAll(Ship(submarine2, Ship.ShipSize.Submarine).coordinates)


        val gameBoard = MutableList(10) { MutableList(10) { 0 } }
        for (coordinate in coordinates) {
            gameBoard[coordinate.first][coordinate.second] = 1
        }

        val playerState = if (playerStatesFromVault.isEmpty()) {
            PrivateGameState(gameBoard, listOf(ourIdentity))
        } else {
            playerStatesFromVault[0].state.data.copy(board = gameBoard)
        }

        // Write the new player state in a transaction
        val transactionBuilder: TransactionBuilder = TransactionBuilder(notary)
                .addOutputState(playerState)
                .addCommand(PrivateGameContract.Commands.AddBattleShips(), listOf(ourIdentity.owningKey))
        if (playerStatesFromVault.isNotEmpty()) {
            transactionBuilder.addInputState(playerStatesFromVault[0])
        }
        transactionBuilder.verify(serviceHub)
        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)
        return subFlow(FinalityFlow(signedTransaction, emptyList()))
    }
}