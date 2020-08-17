package com.cordships.flows

import co.paralleluniverse.fibers.Suspendable
import com.cordships.contracts.GameStateContract
import com.cordships.states.GameState
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.InvalidParameterException
import kotlin.reflect.KClass

object AttackFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            val x: Int,
            val y: Int,
            val gameId: String,
            val adversary: Party
    ) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {

            val me = serviceHub.myInfo.legalIdentities.first()

            val gameStateAndRef = loadInput(GameState::class, UniqueIdentifier.fromString(gameId))
            val gameState = gameStateAndRef.state.data

            // get the move number from the game state
            val outcome = subFlow(HitQueryFlow.Initiator(adversary, x, y, 0, gameId))
                    ?: throw InvalidParameterException("The answer was already requested once")

            // modify the game state with the outcome, next player, next move, etc.

            val notary = serviceHub.networkMapCache.notaryIdentities.single()

            val txCommand = Command(GameStateContract.Commands.Attack(
                    x,
                    y,
                    me,
                    adversary,
                    outcome
            ), listOf(me.owningKey, adversary.owningKey))
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(gameStateAndRef)
                    .addOutputState(gameState, GameStateContract.ID)
                    .addCommand(txCommand)

            txBuilder.verify(serviceHub)

            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            val otherPartySession = initiateFlow(adversary)

            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartySession)))

            return subFlow(FinalityFlow(fullySignedTx, setOf(otherPartySession)))
        }

        private fun <T: ContractState> loadInput(type: KClass<out T>, identifier: UniqueIdentifier): StateAndRef<T> {
            val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(identifier), status= Vault.StateStatus.UNCONSUMED)
            return serviceHub.vaultService.queryBy(type.java, criteria).states.single()
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



