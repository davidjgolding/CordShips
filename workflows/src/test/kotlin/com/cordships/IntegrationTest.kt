package com.cordships

import com.cordships.flows.*
import com.cordships.states.GameStatus
import com.cordships.states.HitOrMiss
import com.cordships.states.PrivateGameState
import com.cordships.states.PublicGameState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class IntegrationTest: AbstractTestClass() {
    @Test
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

    @Test
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
        val attackFlow = AttackFlow.Initiator(shots.toList(), gameId)
        return startFlow(attackFlow).getOrThrow()
    }
}

fun StartedMockNode.defaultIdentity() = this.info.legalIdentities.first()