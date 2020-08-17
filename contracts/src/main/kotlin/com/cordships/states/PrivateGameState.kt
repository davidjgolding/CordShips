package com.cordships.states

import com.cordships.Board
import com.cordships.contracts.PrivateGameContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

/**
 * This class represents a players private view of the pieces they've placed on the board. Players
 * will self-issue this state at the beginning of each game to designate where they wish to place their
 * pieces.
 *
 * @param board The constructed board represented as an array of arrays (grid) indicatign where pieces have been
 * places
 * @param participants The players that should be able to propose updates to this state
 */
@BelongsToContract(PrivateGameContract::class)
data class PrivateGameState(
        val board: Board,
        override val participants: List<AbstractParty> = listOf()
) : ContractState
