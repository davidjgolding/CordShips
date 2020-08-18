package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.states.HitOrMiss
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap

object HitQueryFlow {
    @InitiatingFlow
    class Initiator(
            val adversary: Party,
            val coordinates: Pair<Int,Int>,
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

            val queryId = QueryId(request.turnCount, request.gameStateId)

            val previousOutcome = queries[queryId]
            // proper verification will require
            // 1. recording of that info in the database
            // 2. loading the game state ans see whenever the moveNumber matches to the game
            if (previousOutcome != null) {
                otherPartySession.send(AttackOutcome(
                        request.coordinates,
                        request.turnCount,
                        request.gameStateId,
                        request.attacker,
                        me,
                        previousOutcome))
            } else {
                val game = serviceHub.loadPublicGameState(request.gameStateId).state.data

                if(game.turnCount != request.turnCount || game.getCurrentPlayerParty() != request.attacker) {
                    queries[queryId] = HitOrMiss.UNKNOWN
                    otherPartySession.send(AttackOutcome(
                            request.coordinates,
                            request.turnCount,
                            request.gameStateId,
                            request.attacker,
                            me,
                            HitOrMiss.UNKNOWN))
                }

                val privateBoard = serviceHub.loadPrivateGameState(request.gameStateId).state.data
                val hitOrMiss = privateBoard.isHitOrMiss(request.coordinates)
                queries[queryId] = hitOrMiss
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
            val coordinates: Pair<Int,Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier,
            val attacker: Party
    )

    @CordaSerializable
    data class AttackOutcome(
            val coordinates: Pair<Int,Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier,
            val attacker: Party,
            val adversary: Party,
            val hitOrMiss: HitOrMiss
    )
}


