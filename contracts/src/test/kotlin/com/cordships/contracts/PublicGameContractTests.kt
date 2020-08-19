package com.cordships.contracts

import com.cordships.contracts.PublicGameContract.Commands.*
import com.cordships.contracts.PublicGameContract.Companion.ID
import com.cordships.states.GameStatus
import com.cordships.states.HitOrMiss
import com.cordships.states.PublicGameState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PublicGameContractTests {
    private val ledgerServices = MockServices(listOf("com.cordships.contracts"))
    private val partyA = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
    private val partyB = TestIdentity(CordaX500Name("PartyB", "London", "GB"))
    private val participants = setOf(partyA.party, partyB.party)

    @Test
    fun `Issue game transaction`() {
        ledgerServices.ledger {
            transaction {
                command(participants.map { it.owningKey }, IssueGame())
                output(ID, "publicGame", PublicGameState(participants))
                tweak {
                    input(ID, PublicGameState(participants))
                    fails()
                }
                tweak {
                    output(ID, PublicGameState(participants))
                    fails()
                }
                verifies()
            }

            val unstartedGameStateAndRef = "publicGame".outputStateAndRef<PublicGameState>()
            assertEquals(GameStatus.GAME_NOT_STARTED, unstartedGameStateAndRef.state.data.status)
            assertEquals(2, unstartedGameStateAndRef.state.data.participants.size)
            assertEquals(2, unstartedGameStateAndRef.state.data.playerBoards.values.size)
            assertEquals(0, unstartedGameStateAndRef.state.data.turnCount)
            assertNull(unstartedGameStateAndRef.state.data.playerProofs)
        }
    }

    @Test
    fun `Start game transaction`() {
        ledgerServices.ledger {
            transaction("IssueGame") {
                command(participants.map { it.owningKey }, IssueGame())
                output(ID, "publicGame", PublicGameState(participants))
                verifies()
            }
            val unstartedGameStateAndRef = "publicGame".outputStateAndRef<PublicGameState>()

            transaction("StartGame") {
                input(unstartedGameStateAndRef.ref)
                command(partyA.publicKey, StartGame())
                output(ID, "startedGame", unstartedGameStateAndRef.state.data.copy(status = GameStatus.GAME_IN_PROGRESS, playerProofs = emptyMap()))
                tweak {
                    output(ID, unstartedGameStateAndRef.state.data.copy())
                    fails()
                }
                verifies()
            }

            val startedGameStateAndRef = "startedGame".outputStateAndRef<PublicGameState>()
            assertEquals(GameStatus.GAME_IN_PROGRESS, startedGameStateAndRef.state.data.status)
            assertNotEquals(unstartedGameStateAndRef.state.data.status, startedGameStateAndRef.state.data.status)
            assertNotNull(startedGameStateAndRef.state.data.playerProofs)
            assertEquals(0, startedGameStateAndRef.state.data.turnCount)
        }
    }

    @Test
    fun `Attack transaction`() {
        ledgerServices.ledger {
            transaction("IssueGame") {
                command(participants.map { it.owningKey }, IssueGame())
                output(ID, "publicGame", PublicGameState(participants))
                verifies()
            }
            val unstartedGameStateAndRef = "publicGame".outputStateAndRef<PublicGameState>()

            transaction("StartGame") {
                input(unstartedGameStateAndRef.ref)
                command(partyA.publicKey, StartGame())
                output(ID, "startedGame", unstartedGameStateAndRef.state.data.copy(status = GameStatus.GAME_IN_PROGRESS, playerProofs = emptyMap()))
                verifies()
            }

            val startedGameStateAndRef = "startedGame".outputStateAndRef<PublicGameState>()
            assertEquals(GameStatus.GAME_IN_PROGRESS, startedGameStateAndRef.state.data.status)

            val newState = startedGameStateAndRef.state.data.copy(turnCount = 1)
            newState.playerBoards[partyB.party]?.get(0)?.set(0, HitOrMiss.HIT)

            transaction {
                input(startedGameStateAndRef.ref)
                output(ID, "attackResponse", newState)
                tweak {
                    // wrong party under attack
                    command(partyA.publicKey, Attack(listOf(Shot(Pair(0, 0), partyA.party, HitOrMiss.HIT)), partyA.party))
                    fails()
                }
                tweak {
                    // wrong coordinates
                    command(partyA.publicKey, Attack(listOf(Shot(Pair(9, 9), partyB.party, HitOrMiss.HIT)), partyA.party))
                    fails()
                }
                tweak {
                    // wrong HitOrMiss
                    command(partyA.publicKey, Attack(listOf(Shot(Pair(0, 0), partyB.party, HitOrMiss.MISS)), partyA.party))
                    fails()
                }
                tweak {
                    // wrong number of attacks compared to number of board changes
                    command(partyA.publicKey, Attack(listOf(Shot(Pair(0, 0), partyB.party, HitOrMiss.HIT),
                            Shot(Pair(0, 1), partyB.party, HitOrMiss.HIT)), partyA.party))
                    fails()
                }
                command(partyA.publicKey, Attack(listOf(Shot(Pair(0, 0), partyB.party, HitOrMiss.HIT)), partyA.party))
                verifies()
            }

            val startedGameAfterAttackStateAndRef = "attackResponse".outputStateAndRef<PublicGameState>()
            assertNotEquals(GameStatus.GAME_NOT_STARTED, startedGameAfterAttackStateAndRef.state.data.status)
            assertEquals(startedGameStateAndRef.state.data.playerProofs, startedGameAfterAttackStateAndRef.state.data.playerProofs)
        }
    }
}