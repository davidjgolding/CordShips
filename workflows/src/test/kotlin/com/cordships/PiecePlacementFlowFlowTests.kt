package com.cordships

import com.cordships.flows.IssuePublicGameFlow
import com.cordships.flows.PiecePlacementFlow
import net.corda.core.utilities.getOrThrow
import org.junit.After
import org.junit.Test

class PiecePlacementFlowFlowTests: AbstractTestClass() {
    @Test
    fun `Basic set of ships all going south from the top row`() {
        val gameBoard = a.startFlow(IssuePublicGameFlow(listOf(a.info.legalIdentities.first(), b.info.legalIdentities.first()))).getOrThrow()
        val privateGameState  = a.startFlow(PiecePlacementFlow(gameBoard.linearId,
                listOf(
                "A0S", "B0S", "C0S",
                "D0S", "E0S", "F0S", "G0S"))).getOrThrow()
    }

    @Test
    fun `Basic set of ships all going north from the bottom row`() {
        val gameBoard = a.startFlow(IssuePublicGameFlow(listOf(a.info.legalIdentities.first(), b.info.legalIdentities.first()))).getOrThrow()
        val result = a.startFlow(PiecePlacementFlow(gameBoard.linearId,listOf("A9N", "B9N", "C9N",
                "D9N", "E9N", "F9N", "G9N"))).getOrThrow()
    }

    @Test
    fun `Basic set of ships all going east from the left column`() {
        val gameBoard = a.startFlow(IssuePublicGameFlow(listOf(a.info.legalIdentities.first(), b.info.legalIdentities.first()))).getOrThrow()
        val result = a.startFlow(PiecePlacementFlow(gameBoard.linearId,listOf("A0E", "A1E", "A2E",
                "A3E", "A4E", "A5E", "A6E"))).get()
    }

    @Test
    fun `Basic set of ships all going west from the right column`() {
        val gameBoard = a.startFlow(IssuePublicGameFlow(listOf(a.info.legalIdentities.first(), b.info.legalIdentities.first()))).getOrThrow()
        val result = a.startFlow(PiecePlacementFlow(gameBoard.linearId,listOf("J0W", "J1W", "J2W",
                "J3W", "J4W", "J5W", "J6W"))).get()
    }
}