package com.cordships;

import com.cordships.flows.InitiatePublicGameFlow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class InitiatePublicGameFlowFlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.cordships.contracts"),
            TestCordapp.findCordapp("com.cordships.flows")
    )))
    private val a = network.createNode()
    private val b = network.createNode()
    private val aIdentity = a.info.legalIdentities.first()
    private val bIdentity = b.info.legalIdentities.first()

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `first player is node starting game`() {
        val players = listOf(bIdentity)
        val result = a.startFlow(InitiatePublicGameFlow(players)).get()

        Assert.assertEquals(aIdentity, result.participants.first())
    }

    @Test
    fun `second player is node b`() {
        val players = listOf(bIdentity)
        val result = a.startFlow(InitiatePublicGameFlow(players)).get()

        Assert.assertEquals(bIdentity, result.participants.last())
    }

}
