package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.PublicGameContract
import com.cordships.states.GameStatus
import com.cordships.states.HitOrMiss
import com.cordships.states.PublicGameState
import net.corda.core.contracts.*
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
                throw InvalidParameterException("Please define the shots for the attack.")
            }

            if(shots.map { it.adversary }.distinct().size < shots.size) {
                throw InvalidParameterException("There are duplicate adversaries in the attack.")
            }

            val me = serviceHub.myInfo.legalIdentities.first()

            val gameStateAndRef = serviceHub.loadPublicGameState(gameStateId)
            val gameState = gameStateAndRef.state.data

            if (gameState.status == GameStatus.GAME_NOT_STARTED) {
                throw InvalidParameterException("The game haven't been started yet.")
            }
            else if (gameState.status == GameStatus.GAME_OVER) {
                throw InvalidParameterException("The game is over.")
            }

            if (gameState.getCurrentPlayerParty() != me) {
                throw InvalidParameterException("It's not my turn to play.")
            }

            if(shots.any { it.adversary == me }) {
                throw InvalidParameterException("You cannot attack yourself.")
            }

            val outcomes = shots.map {
                val hitOrMiss = subFlow(HitQueryFlow.Initiator(it.adversary, it.coordinates, gameState.turnCount, gameStateId))
                if (hitOrMiss == HitOrMiss.UNKNOWN) {
                    throw InvalidParameterException("The answer was already requested once.")
                }
                PublicGameContract.Commands.Shot(it.coordinates, it.adversary, hitOrMiss)
            }

            val outcomeStateRefs = shots.map {
                serviceHub.loadHitResponseState(gameStateId, it.adversary, gameState.turnCount)
                        ?: throw InvalidParameterException("Didn't find the response hit state.")
            }

            var playedGameState = gameState
            outcomes.forEach {
                playedGameState = playedGameState.updateBoardWithAttack(it.coordinates, it.adversary, it.hitOrMiss)
            }
            playedGameState = playedGameState.endTurn()


            val publicKeys = gameState.participants.map { it.owningKey }

            val notary = serviceHub.defaultNotary()
            val txCommand = Command(PublicGameContract.Commands.Attack(outcomes, me), publicKeys)
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(gameStateAndRef)
                    .addOutputState(playedGameState, PublicGameContract.ID)
                    .addCommand(txCommand)

            outcomeStateRefs.forEach {
                txBuilder.addReferenceState(ReferencedStateAndRef(it))
            }

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
                    val game = stx.tx.outputs.single().data as PublicGameState
                    val command = stx.tx.commands.single().value
                    if(command is PublicGameContract.Commands.Attack) {
                        val me = serviceHub.myInfo.legalIdentities.first()
                        val shotsToMe = command.shots.filter { it.adversary == me }
                        if(shotsToMe.isNotEmpty()) {
                            val privateBoard = serviceHub.loadPrivateGameState(game.linearId).state.data
                            val myPublicBoard = game.playerBoards.getValue(me)
                            shotsToMe.forEach {
                                "The shot's 'hit or miss' must match my private board state." using (it.hitOrMiss == privateBoard.isHitOrMiss(it.coordinates))
                                "The shot's 'hit or miss' must match my public board state." using (it.hitOrMiss == myPublicBoard[it.coordinates.first][it.coordinates.second])
                            }
                        }
                    }
                }
            }

            val txId = subFlow(signTransactionFlow).id

            subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}



