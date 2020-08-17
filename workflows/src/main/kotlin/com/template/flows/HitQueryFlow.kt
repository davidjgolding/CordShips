package com.template.flows

import co.paralleluniverse.fibers.Suspendable
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
            val x: Int,
            val y: Int,
            val moveNumber: Int,
            val gameId: String
    ) : FlowLogic<Boolean?>() {
        @Suspendable
        override fun call(): Boolean? {
            val me = serviceHub.myInfo.legalIdentities.first()
            initiateFlow(adversary).sendAndReceive<AttackOutcome>(AttackCoordinates(x, y, moveNumber, gameId, me)).unwrap {
                return it.isHit
            }
        }
    }

    @InitiatedBy(Initiator::class)
    class QueryHandler(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
        companion object {
            val queries = HashSet<QueryId>()
        }
        @Suspendable
        override fun call() {
            val me = serviceHub.myInfo.legalIdentities.first()
            val request = otherPartySession.receive<AttackCoordinates>().unwrap { it }
            val queryId = QueryId(request.moveNumber, request.gameId)
            if (queries.contains(queryId)) {
                otherPartySession.send(AttackOutcome(request.x, request.y, request.moveNumber, request.gameId, request.attacker, me, null))
            } else {
                // TODO - add the actual outcome
                // queries.add(request.moveNumber)
                otherPartySession.send(AttackOutcome(request.x, request.y, request.moveNumber, request.gameId, request.attacker, me, true))
            }
        }
    }

    data class QueryId(
            val moveNumber: Int,
            val gameId: String
    )

    @CordaSerializable
    data class AttackCoordinates(
            val x: Int,
            val y: Int,
            val moveNumber: Int,
            val gameId: String,
            val attacker: Party
    )

    @CordaSerializable
    data class AttackOutcome(
            val x: Int,
            val y: Int,
            val moveNumber: Int,
            val gameId: String,
            val attacker: Party,
            val adversary: Party,
            val isHit: Boolean?
    )
}


