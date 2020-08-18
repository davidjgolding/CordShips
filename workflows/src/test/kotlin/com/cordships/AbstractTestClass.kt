package com.cordships

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.BeforeAll

abstract class AbstractTestClass {
    private val network: MockNetwork = MockNetwork(
            MockNetworkParameters(
                    threadPerNode = true,
                    networkParameters = testNetworkParameters(minimumPlatformVersion = 5),
                    notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB"))),
                    cordappsForAllNodes = listOf(
                            TestCordapp.findCordapp("com.cordships.contracts"),
                            TestCordapp.findCordapp("com.cordships.flows")
                    )
            )
    )

    val a: StartedMockNode = network.createNode(MockNodeParameters())
    val b: StartedMockNode = network.createNode(MockNodeParameters())
    val c: StartedMockNode = network.createNode(MockNodeParameters())
    val d: StartedMockNode = network.createNode(MockNodeParameters())

    val partyA = a.info.singleIdentity()
    val partyB = b.info.singleIdentity()
    val partyC = c.info.singleIdentity()
    val partyD = d.info.singleIdentity()

    @Before
    fun setup() {
        network.startNodes()
    }

    @After
    fun cleanup() {
        network.stopNodes()
    }
}