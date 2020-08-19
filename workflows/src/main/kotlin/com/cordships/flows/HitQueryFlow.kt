package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.states.HitOrMiss
import com.cordships.states.HitResponseState
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
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier
    ) : FlowLogic<HitOrMiss>() {
        @Suspendable
        override fun call(): HitOrMiss {
            val me = serviceHub.myInfo.legalIdentities.first()
            initiateFlow(adversary).sendAndReceive<HitResponseState>(
                    AttackCoordinates(coordinates, turnCount, gameStateId, me)).unwrap {
                return it.hitOrMiss
            }
        }
    }

    @InitiatedBy(Initiator::class)
    class QueryHandler(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val me = serviceHub.myInfo.legalIdentities.first()
            val request = otherPartySession.receive<AttackCoordinates>().unwrap { it }
            val queryState = subFlow(HitResponseFlow.Initiator(request.attacker, me, request.coordinates, request.turnCount, request.gameStateId))
            otherPartySession.send(queryState)
        }
    }

    @CordaSerializable
    data class AttackCoordinates(
            val coordinates: Pair<Int, Int>,
            val turnCount: Int,
            val gameStateId: UniqueIdentifier,
            val attacker: Party
    )
}


