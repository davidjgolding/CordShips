package com.cordships

import com.cordships.flows.PiecePlacement
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

class PricePlacementFlowTests {
    private val network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
    )))
    private val a = network.createNode()

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `Basic set of ships all going south from the top row`() {
        val result = a.startFlow(PiecePlacement("A0S", "B0S", "C0S",
                "D0S", "E0S", "F0S", "G0S")).get()
    }

    @Test
    fun `Basic set of ships all going north from the bottom row`() {
        val result = a.startFlow(PiecePlacement("A9N", "B9N", "C9N",
                "D9N", "E9N", "F9N", "G9N")).get()
    }

    @Test
    fun `Basic set of ships all going east from the left column`() {
        val result = a.startFlow(PiecePlacement("A0E", "A1E", "A2E",
                "A3E", "A4E", "A5E", "A6E")).get()
    }

    @Test
    fun `Basic set of ships all going west from the right column`() {
        val result = a.startFlow(PiecePlacement("J0W", "J1W", "J2W",
                "J3W", "J4W", "J5W", "J6W")).get()
    }

}