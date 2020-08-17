package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class PlayerState(val battleShips: List<Pair<Int,Int>>, override val participants: List<AbstractParty> = listOf()) : ContractState
