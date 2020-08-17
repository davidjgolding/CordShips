package com.cordships.states

import com.cordships.contracts.GameStateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

@BelongsToContract(GameStateContract::class)
class GameState(override val participants: List<AbstractParty> = listOf()) : ContractState
