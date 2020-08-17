package com.cordships.states

import com.cordships.contracts.AttackContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(AttackContract::class)
data class AttackState(
        val x: Int,
        val y: Int,
        val attacker: Party,
        val adversary: Party,
        val isHit: Boolean,
        override val participants: List<AbstractParty> = listOf(attacker, adversary)
) : ContractState
