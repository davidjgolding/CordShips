package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PublicGameContract
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

/**
 * This flow is used to start a game with counter parties. It requires counter parties to generate
 * their private views of the game and share proof that they have made decisions concerning their piece
 * placement.
 *
 * Consumes: Unstarted [PublicGameState]
 * Produces: Started [PublicGameState]
 *
 * @param gameStateId The linearId of the game state in question
 */

@StartableByRPC
@InitiatingFlow
class StartGameFlow(private val gameStateId: UniqueIdentifier) : FlowLogic<PublicGameState>() {
    @Suspendable
    override fun call(): PublicGameState {

        // Find the instantiated game
        val unstartedGameStateAndRef = serviceHub.vaultService.queryBy<PublicGameState>(
                QueryCriteria.LinearStateQueryCriteria(linearId = listOf(gameStateId))
        ).states.single()
        val unstartedGameState = unstartedGameStateAndRef.state.data

        // Retrieve PROOF from the counter parties of their piece positions
        val otherPlayers = unstartedGameState.playerBoards.keys - ourIdentity
        val otherPlayerSessions = mutableListOf<FlowSession>()
        val playerProofs = otherPlayers.map {
            val session = initiateFlow(it).also { session -> otherPlayerSessions.add(session) }
            it to session.sendAndReceive<Int>(gameStateId).unwrap { data -> data }
        }.toMap()

        // Create a started game board with PROOF retrieved from each of the players
        val startedGame = unstartedGameState.startGame(playerProofs = playerProofs)

        // Instantiate a transaction builder, add input / output states and commands
        val tb = TransactionBuilder(serviceHub.defaultNotary()).apply {
            addInputState(unstartedGameStateAndRef)
            addOutputState(startedGame)
            addCommand(
                    PublicGameContract.Commands.StartGame(),
                    otherPlayers.map { it.owningKey }
            )
        }

        // Build a transaction, collect signatures
        val ptx = serviceHub.signInitialTransaction(tb)
        val stx = subFlow(CollectSignaturesFlow(
                ptx,
                otherPlayerSessions
        ))
        val ftx = subFlow(FinalityFlow(stx, otherPlayerSessions))

        // Return the instantiated, un-started game state
        return stx.coreTransaction.outputsOfType<PublicGameState>().single()
    }
}

/** A flow responder designed to provide proof of places pieces. */
@InitiatedBy(StartGameFlow::class)
class StartGameFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Find the private board view related to the unstarted game
        val gameStateId = otherPartySession.receive<UniqueIdentifier>().unwrap { it }
        val privateGameStateAndRef = serviceHub.vaultService.queryBy<PrivateGameState>()
                .states.single { it.state.data.associatedPublicGameState == gameStateId }
        val privateGameState = privateGameStateAndRef.state.data

        // Send proof of a signed game state without revealing its contents
        otherPartySession.send(privateGameState.hashCode())

        // Sign the transaction / receive the finalized transaction
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txId = subFlow(signTransactionFlow).id
        subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}
