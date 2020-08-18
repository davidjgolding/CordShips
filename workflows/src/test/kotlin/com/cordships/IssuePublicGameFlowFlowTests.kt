package com.cordships;

import com.cordships.flows.IssuePublicGameFlow
import com.cordships.states.PublicGameState
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class IssuePublicGameFlowFlowTests: AbstractTestClass() {
    private val aIdentity = a.info.legalIdentities.first()
    private val bIdentity = b.info.legalIdentities.first()

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `A game state is successfully issued`() {
        val players = listOf(aIdentity, bIdentity)
        val result = a.startFlow(IssuePublicGameFlow(players)).getOrThrow()

        assertTrue(result.playerBoards.keys.containsAll(players), "Players should be included in the game board")
    }

}
