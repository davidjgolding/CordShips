package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PrivateGameContract
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import com.cordships.states.Ship
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/** Places all pieces on a game board. */
@InitiatingFlow
@StartableByRPC
class PiecePlacementFlow(
        private val gameBoardId: UniqueIdentifier,
        private val airCraftCarrier: String,
        private val battleship: String,
        private val cruiser: String,
        private val destroyer1: String,
        private val destroyer2: String,
        private val submarine1: String,
        private val submarine2: String
) : FlowLogic<PrivateGameState>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): PrivateGameState {

        // Get already existing game board
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(gameBoardId))
        val publicGameStateAndRef = serviceHub.vaultService.queryBy<PublicGameState>(criteria).states.single()
        val publicGameState = publicGameStateAndRef.state.data

        // Get points/coordinates for all inputs
        val ships = listOf(
            Ship(airCraftCarrier, Ship.ShipSize.AirCraftCarrier),
            Ship(battleship, Ship.ShipSize.BattleShip),
            Ship(cruiser, Ship.ShipSize.Cruiser),
            Ship(destroyer1, Ship.ShipSize.Destroyer),
            Ship(destroyer2, Ship.ShipSize.Destroyer),
            Ship(submarine1, Ship.ShipSize.Submarine),
            Ship(submarine2, Ship.ShipSize.Submarine)
        )

        // Create and issue a private game board state
        val privateGameState = PrivateGameState(ships, ourIdentity, publicGameState.linearId)
        val tb = TransactionBuilder(serviceHub.defaultNotary()).apply {
            addReferenceState(ReferencedStateAndRef(publicGameStateAndRef))
            addOutputState(privateGameState)
            addCommand(PrivateGameContract.Commands.IssuePrivateGameState(), listOf(ourIdentity.owningKey))
        }

        // Build a transaction, collect signatures
        val stx = serviceHub.signInitialTransaction(tb)
        val ftx = subFlow(FinalityFlow(stx, listOf()))
        return ftx.coreTransaction.outputsOfType<PrivateGameState>().single()
    }
}

