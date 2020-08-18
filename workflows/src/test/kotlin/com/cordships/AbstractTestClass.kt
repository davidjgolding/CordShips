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
    lateinit var network: MockNetwork

    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode
    lateinit var d: StartedMockNode

    lateinit var partyA: Party
    lateinit var partyB: Party
    lateinit var partyC: Party
    lateinit var partyD: Party

    @Before
    fun setup() {
        network = MockNetwork(
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

        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters())
        c = network.createNode(MockNodeParameters())
        d = network.createNode(MockNodeParameters())

        partyA = a.info.singleIdentity()
        partyB = b.info.singleIdentity()
        partyC = c.info.singleIdentity()
        partyD = d.info.singleIdentity()
    }

    @After
    fun cleanup() {
        network.stopNodes()
    }
}