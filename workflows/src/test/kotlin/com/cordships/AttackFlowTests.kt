package com.cordships

import com.cordships.flows.AttackFlow
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import org.junit.After
import org.junit.Before
import org.junit.Test

class AttackFlowTests: AbstractTestClass() {
    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `dummy test`() {
        val flow = AttackFlow.Initiator(1, 1, "", b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(b.info.singleIdentity().owningKey)
    }
}