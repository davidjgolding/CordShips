package com.cordships.states

import com.cordships.Board
import com.cordships.contracts.PrivateGameContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
@BelongsToContract(PrivateGameContract::class)
data class PrivateGameState(val board: Board, override val participants: List<AbstractParty> = listOf()) : ContractState
