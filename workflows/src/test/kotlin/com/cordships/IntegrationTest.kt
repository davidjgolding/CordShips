package com.cordships

import com.cordships.flows.*
import com.cordships.states.*
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.StartedMockNode
import org.junit.Test
import java.util.*
import kotlin.test.*

class IntegrationTest: AbstractTestClass() {
    @Test(timeout = 30_000)
    fun `happy path able to issue, setup game board correctly and play random two first rounds`() {

        val data = startGame()

        // turn 1
        var game = a.play(data.first.linearId, Shot(Pair(1,1), partyB), Shot(Pair(2,2), partyD))

        assertEquals(GameStatus.GAME_IN_PROGRESS, game.status)
        assertEquals(1, game.turnCount)
        assertEquals(0, game.playerBoards.getValue(partyA).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(0, game.playerBoards.getValue(partyC).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyB).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyD).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyB)[1][1])
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyD)[2][2])

        // turn 2
        game = b.play(data.first.linearId, Shot(Pair(3,3), partyA), Shot(Pair(4,4), partyD))

        assertEquals(GameStatus.GAME_IN_PROGRESS, game.status)
        assertEquals(2, game.turnCount)
        assertEquals(1, game.playerBoards.getValue(partyA).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyB).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(0, game.playerBoards.getValue(partyC).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertEquals(2, game.playerBoards.getValue(partyD).sumBy {
            it.sumBy { c -> if( c != HitOrMiss.UNKNOWN) 1 else 0 }
        })
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyA)[3][3])
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyB)[1][1])
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyD)[2][2])
        assertNotEquals(HitOrMiss.UNKNOWN, game.playerBoards.getValue(partyD)[4][4])
    }

    @Test(timeout = 30_000)
    fun `hitting ships`() {

        val data = startGame()

        val shipsB = data.second.first { it.owner == partyB }.board
        val shipsD = data.second.first { it.owner == partyD }.board

        // turn 1
        var game = a.play(data.first.linearId,
                Shot(shipsB[0].coordinates[0], partyB),
                Shot(shipsD[0].coordinates[0], partyD))

        assertEquals(GameStatus.GAME_IN_PROGRESS, game.status)
        assertEquals(1, game.turnCount)
        assertEquals(0, game.playerBoards.getValue(partyA).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(0, game.playerBoards.getValue(partyC).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyB).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyD).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })

        // turn 2
        game = b.play(data.first.linearId,
                Shot(shipsB[0].coordinates[0], partyA),
                Shot(shipsD[1].coordinates[0], partyD))

        assertEquals(GameStatus.GAME_IN_PROGRESS, game.status)
        assertEquals(2, game.turnCount)
        assertEquals(1, game.playerBoards.getValue(partyA).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(1, game.playerBoards.getValue(partyB).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(0, game.playerBoards.getValue(partyC).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
        assertEquals(2, game.playerBoards.getValue(partyD).sumBy {
            it.sumBy { c -> if( c == HitOrMiss.HIT) 1 else 0 }
        })
    }

    @Test(timeout = 300_000)
    fun `playing until completion`() {

        val data = startGame()

        val s = Stack<String>()
        val ships = mapOf(
                partyA to data.second.first { it.owner == partyA }.board.flatMap { it.coordinates }.toStack(),
                partyB to data.second.first { it.owner == partyB }.board.flatMap { it.coordinates }.toStack(),
                partyC to data.second.first { it.owner == partyC }.board.flatMap { it.coordinates }.toStack(),
                partyD to data.second.first { it.owner == partyD }.board.flatMap { it.coordinates }.toStack()
        )

        val attackers = mapOf<AbstractParty, StartedMockNode>(
                partyA to a,
                partyB to b,
                partyC to c,
                partyD to d
        )
        var attacker = a
        var game: PublicGameState? = null
        var expectedTurnCount = 0
        while(true) {
            val shots = mutableListOf<Shot>()
            ships.keys.filter { it != attacker.info.singleIdentity() }.forEach {
                if(shots.size == 3) {
                    return@forEach
                }
                if(!ships.getValue(it).empty()) {
                    shots.add(Shot(ships.getValue(it).pop(), it))
                }
            }

            if(shots.isEmpty()) {
                fail("The game must be over before we run out of shots")
            }

            game = attacker.play(data.first.linearId, *shots.toTypedArray())

            if(game.isGameOver())
            {
                println("GAME IS OVER in $expectedTurnCount turns, the winner is: ${game.getWinner()}")
                assertEquals(expectedTurnCount, game.turnCount)
                assertNotNull(game.getWinner())
                break
            }

            println("AFTER GAME TURN: $expectedTurnCount")
            game.playerBoards.map { Pair(it.key, it.value.isGameOver()) }.forEach {
                println("PARTY: ${it.first.name} is the game over ${it.second}")
            }

            expectedTurnCount++
            assertEquals(expectedTurnCount, game.turnCount)

            attacker = attackers.getValue(game.getCurrentPlayerParty())
        }
    }

    private fun startGame(): Pair<PublicGameState, List<PrivateGameState>> {
        val testNodes = listOf(a, b, c, d)

        // Issue a public game board
        val publicGame = a.startFlow(IssuePublicGameFlow(testNodes.map { it.defaultIdentity() })).getOrThrow()

        // Issue all private boards
        val privateGameStates = testNodes.map {
            it.startFlow(PiecePlacementFlow(publicGame.linearId,
                    "A0S", "B0S", "C0S",
                    "D0S", "E0S", "F0S", "G0S")).getOrThrow()
        }

        // Start the game
        val startedGame = a.startFlow(StartGameFlow(publicGame.linearId)).getOrThrow()
        assertEquals(GameStatus.GAME_IN_PROGRESS, startedGame.status)
        assertEquals(0, startedGame.turnCount)
        return Pair(publicGame, privateGameStates)
    }

    private fun StartedMockNode.play(gameId: UniqueIdentifier, vararg shots: Shot): PublicGameState {
        println("THE SHOOTING PARTY: ${info.singleIdentity().name}")
        shots.forEach {
            println("ADVERSARY: ${it.adversary.name}")
        }
        val attackFlow = AttackFlow.Initiator(shots.toList(), gameId)
        return startFlow(attackFlow).getOrThrow()
    }

    private fun <T> List<T>.toStack(): Stack<T> {
        val stack = Stack<T>()
        forEach { stack.push(it) }
        return stack
    }
}

fun StartedMockNode.defaultIdentity() = this.info.legalIdentities.first()