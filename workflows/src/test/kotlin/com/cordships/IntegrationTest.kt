package com.cordships

import com.cordships.flows.*
import com.cordships.states.GameStatus
import com.cordships.states.HitOrMiss
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.StartedMockNode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IntegrationTest: AbstractTestClass() {
    @Test
    fun `happy path able to issue and setup gameboard correctly`() {
        val testNodes = listOf(a, b, c, d)

        // Issue a public game board
        val publicGame = a.startFlow(IssuePublicGameFlow(testNodes.map { it.defaultIdentity() })).getOrThrow()

        // Issue all private boards
        testNodes.forEach {
            it.startFlow(PiecePlacementFlow(publicGame.linearId,
                    "A0S", "B0S", "C0S",
                    "D0S", "E0S", "F0S", "G0S")).getOrThrow()
        }

        // Start the game
        val startedGame = a.startFlow(StartGameFlow(publicGame.linearId)).getOrThrow()
        assertEquals(GameStatus.GAME_IN_PROGRESS, startedGame.status)
        assertEquals(0, startedGame.turnCount)

        val partyA = a.info.singleIdentity()
        val partyB = b.info.singleIdentity()
        val partyC = c.info.singleIdentity()
        val partyD = d.info.singleIdentity()

        val attackFlow = AttackFlow.Initiator(listOf(Shot(Pair(1, 1), partyB)), publicGame.linearId)
        val afterFirstMoveGame = a.startFlow(attackFlow).getOrThrow()

        assertEquals(GameStatus.GAME_IN_PROGRESS, afterFirstMoveGame.status)
        assertEquals(1, afterFirstMoveGame.turnCount)
        assertEquals(HitOrMiss.UNKNOWN, afterFirstMoveGame.playerBoards.getValue(partyA)[1][1])
        assertNotEquals(HitOrMiss.UNKNOWN, afterFirstMoveGame.playerBoards.getValue(partyB)[1][1])
        assertEquals(HitOrMiss.UNKNOWN, afterFirstMoveGame.playerBoards.getValue(partyC)[1][1])
        assertEquals(HitOrMiss.UNKNOWN, afterFirstMoveGame.playerBoards.getValue(partyD)[1][1])
    }
}

fun StartedMockNode.defaultIdentity() = this.info.legalIdentities.first()