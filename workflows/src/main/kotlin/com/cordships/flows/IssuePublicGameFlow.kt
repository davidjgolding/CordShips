package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PublicGameContract
import com.cordships.states.PublicGameState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

/**
 * The first flow run in Cordships. This flow is used to create a new game and share that
 * game with the counter parties involved.
 *
 * Consumes: Nothing
 * Produces: Unstarted [PublicGameState]
 *
 * @param players The players that should participate.
 */

@StartableByRPC
@InitiatingFlow
class IssuePublicGameFlow(private val players: List<Party>) : FlowLogic<PublicGameState>() {
    @Suspendable
    override fun call(): PublicGameState {
        // Instantiate a new game state
        val newGameState = PublicGameState((players + ourIdentity).toSet())

        // Create a transaction builder, add states + commands
        val tb = TransactionBuilder(serviceHub.defaultNotary()).apply {
            addOutputState(newGameState)
            addCommand(PublicGameContract.Commands.IssueGame(), players.map { it.owningKey })
        }

        // Sign the transaction and then collect signatures from counterparties
        val sessions = (players - ourIdentity).map { initiateFlow(it) }
        val ptx = serviceHub.signInitialTransaction(tb)
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        val ftx = subFlow(FinalityFlow(stx, sessions))

        // Return the instantiated, un-started game state
        return ftx.coreTransaction.outputsOfType<PublicGameState>().single()
    }
}

/** A flow responder designed to record an instantiated game. */
@InitiatedBy(IssuePublicGameFlow::class)
class IssuePublicGameFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txId = subFlow(signTransactionFlow).id
        subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}
