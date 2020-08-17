package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AttackContract
import com.template.states.AttackState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.security.InvalidParameterException

object AttackFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            val x: Int,
            val y: Int,
            val adversary: Party
    ) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {

            val me = serviceHub.myInfo.legalIdentities.first()

            val outcome = subFlow(HitQueryFlow.Initiator(adversary, x, y, 0, ""))
                    ?: throw InvalidParameterException("The answer was already requested once")

            val notary = serviceHub.networkMapCache.notaryIdentities.single()

            val startTurnState = AttackState(x, y, me, adversary, outcome)

            val txCommand = Command(AttackContract.Commands.Start(), startTurnState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(startTurnState, AttackContract.ID)
                    .addCommand(txCommand)

            txBuilder.verify(serviceHub)

            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            val otherPartySession = initiateFlow(adversary)

            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartySession)))

            return subFlow(FinalityFlow(fullySignedTx, setOf(otherPartySession)))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
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
}



