package com.cordships

import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.*

abstract class AbstractTestClass {
    val network = MockNetwork(
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

    val a = network.createNode(MockNodeParameters())
    val b = network.createNode(MockNodeParameters())
    val c = network.createNode(MockNodeParameters())
    val d = network.createNode(MockNodeParameters())
}