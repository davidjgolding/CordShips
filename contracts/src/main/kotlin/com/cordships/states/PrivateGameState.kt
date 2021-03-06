package com.cordships.states

import com.cordships.contracts.PrivateGameContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.security.SecureRandom

/**
 * This class represents a players private view of the pieces they've placed on the board. Players
 * will self-issue this state at the beginning of each game to designate where they wish to place their
 * pieces.
 *
 * @param board The constructed board represented as an array of arrays (grid) indicating where pieces have been placed
 * @param participants The players that should be able to propose updates to this state
 */
@BelongsToContract(PrivateGameContract::class)
data class PrivateGameState(
        val board: List<Ship>,
        val owner: Party,
        val associatedPublicGameState: UniqueIdentifier,
        private val salt: PrivacySalt = PrivacySalt(SecureRandom.getSeed(32)),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState {
    init {
        requireThat {
            "All ships are accounted for with not duplicates" using (board.toSet().size == 7)
            "All ships are appropriately names" using (board.all { safeValueOf<Ship.ShipSize>(it.vesselClass) != null })
        }
    }
    override val participants: List<AbstractParty> = listOf(owner)

    fun isHitOrMiss(coordinates: Pair<Int, Int>) : HitOrMiss {
        return when(board.any {it.coordinates.any { xy -> xy == coordinates } }) {
            true -> HitOrMiss.HIT
            else -> HitOrMiss.MISS
        }
    }
}

/** A utility function to retrieve a safe value from an enum */
inline fun <reified T : kotlin.Enum<T>> safeValueOf(type: String?): T? {
    return java.lang.Enum.valueOf(T::class.java, type)
}
