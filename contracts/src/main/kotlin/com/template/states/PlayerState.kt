package com.cordships.states

import com.cordships.Board
import com.template.contracts.PlayerContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
@BelongsToContract(PlayerContract::class)
data class PlayerState(val board: Board, override val participants: List<AbstractParty> = listOf()) : ContractState
