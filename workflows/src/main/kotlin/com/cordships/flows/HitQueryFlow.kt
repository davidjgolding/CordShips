package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.HitQueryContract
import com.cordships.contracts.PublicGameContract
import com.cordships.states.HitOrMiss
import com.cordships.states.HitQueryState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap

object HitQueryFlow {
    @InitiatingFlow
    class Initiator(
            val adversary: Party,
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier
    ) : FlowLogic<HitOrMiss>() {
        @Suspendable
        override fun call(): HitOrMiss {
            val me = serviceHub.myInfo.legalIdentities.first()
            initiateFlow(adversary).sendAndReceive<AttackOutcome>(
                    AttackCoordinates(coordinates, turnCount, gameStateId, me)).unwrap {
                return it.hitOrMiss
            }
        }
    }

    @InitiatedBy(Initiator::class)
    class QueryHandler(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
        companion object {
            val queries = mutableMapOf<QueryId, HitOrMiss>()
        }

        @Suspendable
        override fun call() {
            val me = serviceHub.myInfo.legalIdentities.first()
            val request = otherPartySession.receive<AttackCoordinates>().unwrap { it }

            var queryState = serviceHub.loadHitQueryState(request.gameStateId, request.turnCount)?.state?.data

            if (queryState != null) {
                otherPartySession.send(AttackOutcome(
                        request.coordinates,
                        request.turnCount,
                        request.gameStateId,
                        request.attacker,
                        me,
                        queryState.hitOrMiss))
            } else {
                val game = serviceHub.loadPublicGameState(request.gameStateId).state.data

                val hitOrMiss = if (game.turnCount != request.turnCount || game.getCurrentPlayerParty() != request.attacker) {
                    HitOrMiss.UNKNOWN
                } else {
                    val privateBoard = serviceHub.loadPrivateGameState(request.gameStateId).state.data
                    privateBoard.isHitOrMiss(request.coordinates)
                }

                queryState = HitQueryState(
                        me,
                        request.gameStateId,
                        request.turnCount,
                        hitOrMiss
                )
                val notary = serviceHub.defaultNotary()
                val txCommand = Command(HitQueryContract.Commands.Issue(), listOf(me.owningKey))
                val txBuilder = TransactionBuilder(notary)
                        .addOutputState(queryState, HitQueryContract.ID)
                        .addCommand(txCommand)
                txBuilder.verify(serviceHub)
                val fullySignedTx = serviceHub.signInitialTransaction(txBuilder)
                subFlow(FinalityFlow(fullySignedTx, listOf()))

                otherPartySession.send(AttackOutcome(request.coordinates, request.turnCount, request.gameStateId, request.attacker, me, hitOrMiss))
            }
        }
    }

    @CordaSerializable
    data class QueryId(
            val turnCount: Int,
            val gameStateId: UniqueIdentifier
    )

    @CordaSerializable
    data class AttackCoordinates(
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier,
            val attacker: Party
    )

    @CordaSerializable
    data class AttackOutcome(
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier,
            val attacker: Party,
            val adversary: Party,
            val hitOrMiss: HitOrMiss
    )
}


