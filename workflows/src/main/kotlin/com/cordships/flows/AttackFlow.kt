package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PublicGameContract
import com.cordships.states.HitOrMiss
import com.cordships.states.PublicGameState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.InvalidParameterException


@CordaSerializable
data class Shot(
        val coordinates: Pair<Int, Int>,
        val adversary: Party
)

object AttackFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val shots: List<Shot>, val gameStateId: UniqueIdentifier) : FlowLogic<PublicGameState>() {
        @Suspendable
        override fun call(): PublicGameState {

            if (shots.isEmpty()) {
                throw InvalidParameterException("Please define the shots for the attach.")
            }

            val me = serviceHub.myInfo.legalIdentities.first()

            val gameStateAndRef = serviceHub.loadPublicGameState(gameStateId)
            val gameState = gameStateAndRef.state.data

            if (gameState.getCurrentPlayerParty() != me) {
                throw InvalidParameterException("It's not my turn to play.")
            }

            val outcomes = shots.map {
                val hitOrMiss = subFlow(HitQueryFlow.Initiator(it.adversary, it.coordinates, gameState.turnCount, gameStateId))
                if (hitOrMiss == HitOrMiss.UNKNOWN) {
                    throw InvalidParameterException("The answer was already requested once.")
                }
                PublicGameContract.Commands.Shot(it.coordinates, it.adversary, hitOrMiss)
            }

            var playedGameState = gameState.endTurn()
            outcomes.forEach {
                playedGameState = playedGameState.updateBoardWithAttack(it.coordinates, it.adversary, it.hitOrMiss)
            }

            val publicKeys = gameState.participants.map { it.owningKey }.toMutableList()

            val notary = serviceHub.defaultNotary()
            val txCommand = Command(PublicGameContract.Commands.Attack(outcomes, me), publicKeys)
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(gameStateAndRef)
                    .addOutputState(playedGameState, PublicGameContract.ID)
                    .addCommand(txCommand)

            txBuilder.verify(serviceHub)

            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            val otherPartySessions = gameState.participants.filter { it != me }.map {
                initiateFlow(it)
            }.toSet()

            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions))

            val tx = subFlow(FinalityFlow(fullySignedTx, otherPartySessions))

            return tx.coreTransaction.outputsOfType<PublicGameState>().single()
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



