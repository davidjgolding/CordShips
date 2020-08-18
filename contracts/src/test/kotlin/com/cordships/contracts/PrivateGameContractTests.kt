package com.cordships.contracts

import com.cordships.contracts.PrivateGameContract.Commands.IssuePrivateGameState
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import com.cordships.states.Ship
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class PrivateGameContractTests {
    private val ledgerServices = MockServices(listOf("com.cordships.contracts"))
    private val partyA = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
    private val board = createTestGameBoard()

    @Test
    fun `valid private game issuance transaction`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                verifies()
            }
        }
    }

    @Test
    fun `private game issuance transaction should not contain input state`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                tweak {
                    input(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                    fails()
                }
                verifies()
            }
        }
    }

    @Test
    fun `private game issuance transaction should contain only one output state`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                    fails()
                }
                verifies()
            }
        }
    }

    @Test
    fun `private game issuance transaction output should be a PrivateGameState`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                tweak {
                    output(PrivateGameContract.ID, PublicGameState(setOf(partyA.party)))
                    fails()
                }
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                verifies()
            }
        }
    }

    @Test
    fun `private game issuance transaction should only contain a single command`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                tweak {
                    command(partyA.publicKey, IssuePrivateGameState())
                    fails()
                }
                verifies()
            }
        }
    }

    @Test
    fun `Incorrect ship configuration for private game issuance transaction`() {
        ledgerServices.ledger {
            transaction {
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(
                            createTestGameBoard(numAirCraftCarrier = 2, numBattleShips = 0),
                            partyA.party, UniqueIdentifier()))
                    fails()
                }
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(
                            createTestGameBoard(numBattleShips = 2, numCruisers = 0),
                            partyA.party, UniqueIdentifier()))
                    fails()
                }
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(
                            createTestGameBoard(numCruisers = 2, numDestroyers = 1),
                            partyA.party, UniqueIdentifier()))
                    fails()
                }
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(
                            createTestGameBoard(numDestroyers = 3, numSubmarines = 1),
                            partyA.party, UniqueIdentifier()))
                    fails()
                }
                tweak {
                    output(PrivateGameContract.ID, PrivateGameState(
                            createTestGameBoard(numDestroyers = 1, numSubmarines = 3),
                            partyA.party, UniqueIdentifier()))
                    fails()
                }
                verifies()
            }
        }
    }

    @Test
    fun `owner must be signer`() {
        ledgerServices.ledger {
            transaction {
                val partyB = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
                tweak {
                    command(partyB.publicKey, IssuePrivateGameState())
                    output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                    fails()
                }
                tweak {
                    command(partyA.publicKey, IssuePrivateGameState())
                    output(PrivateGameContract.ID, PrivateGameState(board, partyB.party, UniqueIdentifier()))
                    fails()
                }
                command(partyA.publicKey, IssuePrivateGameState())
                output(PrivateGameContract.ID, PrivateGameState(board, partyA.party, UniqueIdentifier()))
                verifies()
            }
        }
    }

    private fun createTestGameBoard(numAirCraftCarrier: Int = 1, numBattleShips: Int = 1,
                                    numCruisers: Int = 1, numDestroyers: Int = 2,
                                    numSubmarines: Int = 2): MutableList<Ship> {
        val output = mutableListOf<Ship>()
        output.addAll(buildTestShips(Ship.ShipSize.AirCraftCarrier, 0, numAirCraftCarrier))
        output.addAll(buildTestShips(Ship.ShipSize.BattleShip, numAirCraftCarrier, numBattleShips))
        output.addAll(buildTestShips(Ship.ShipSize.Cruiser, numAirCraftCarrier + numBattleShips, numCruisers))
        output.addAll(buildTestShips(Ship.ShipSize.Destroyer, numAirCraftCarrier + numBattleShips + numCruisers, numDestroyers))
        output.addAll(buildTestShips(Ship.ShipSize.Submarine, numAirCraftCarrier + numBattleShips + numCruisers + numDestroyers, numSubmarines))
        return output
    }

    private fun buildTestShips(shipSize: Ship.ShipSize, startingIndex: Int, numIterations: Int): MutableList<Ship> {
        val output = mutableListOf<Ship>()
        for (i in startingIndex until (startingIndex + numIterations)) {
            output.add(Ship("${('A'.toInt() + i).toChar()}0S", shipSize))
        }
        return output
    }
}