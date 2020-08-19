package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.HitResponseContract
import com.cordships.states.HitResponseState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.InvalidParameterException

object HitResponseFlow {
    @InitiatingFlow
    class Initiator(
            val attacker: Party,
            val adversary: Party,
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier
    ) : FlowLogic<HitResponseState>() {
        @Suspendable
        override fun call(): HitResponseState {
            val me = serviceHub.myInfo.legalIdentities.first()
            if (me != adversary) {
                throw InvalidParameterException("The adversary must be me.")
            }

            var queryState = serviceHub.loadHitResponseState(gameStateId, me, turnCount)?.state?.data

            if (queryState != null) {
                return queryState
            } else {
                val game = serviceHub.loadPublicGameState(gameStateId).state.data

                if (game.turnCount != turnCount || game.getCurrentPlayerParty() != attacker) {
                    throw InvalidParameterException("The request is coming out of turn: game.turnCount != turnCount || game.getCurrentPlayerParty() != attacker (${game.turnCount},  $turnCount, ${game.getCurrentPlayerParty()},  $attacker)")
                }

                val privateBoard = serviceHub.loadPrivateGameState(gameStateId).state.data
                val hitOrMiss = privateBoard.isHitOrMiss(coordinates)

                val notary = serviceHub.defaultNotary()
                val publicKeys = game.participants.map { it.owningKey }.toMutableList()
                queryState = HitResponseState(attacker, me, gameStateId, turnCount, hitOrMiss, game.participants)
                val txCommand = Command(HitResponseContract.Commands.Issue(), publicKeys)
                val txBuilder = TransactionBuilder(notary)
                        .addOutputState(queryState, HitResponseContract.ID)
                        .addCommand(txCommand)

                val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

                val otherPartySessions = game.participants.filter { it != me }.map {
                    initiateFlow(it)
                }.toSet()

                val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions))

                subFlow(FinalityFlow(fullySignedTx, otherPartySessions))

                return queryState
            }
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                }
            }

            val txId = subFlow(signTransactionFlow).id

            val tx = subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))

            val output = tx.tx.outputs.single().data as HitResponseState

            println("Finalized hit response by ${serviceHub.myInfo.legalIdentities.first()} from ${output.attacker} to ${output.owner} in ${output.turnCount} turn")
        }
    }
}


