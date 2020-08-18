package com.cordships

import com.cordships.flows.IssuePublicGameFlow
import com.cordships.flows.PiecePlacementFlow
import com.cordships.flows.StartGameFlow
import com.cordships.states.GameStatus
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import org.junit.Test
import kotlin.test.assertEquals

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
        assertEquals(startedGame.status, GameStatus.GAME_IN_PROGRESS)
    }
}

fun StartedMockNode.defaultIdentity() = this.info.legalIdentities.first()