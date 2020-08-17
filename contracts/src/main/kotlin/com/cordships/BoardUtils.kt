package com.cordships

import com.cordships.states.PublicGameState
import net.corda.core.contracts.UniqueIdentifier

typealias Board = MutableList<MutableList<Int>>

class BoardUtils {
    companion object {

        fun checkIfValidPositions(positions: List<Pair<Int, Int>>) : Boolean {
            for(position in positions) {
                if (!checkIfValidPosition(position))
                    return false
            }
            return true
        }

        private fun checkIfValidPosition(position: Pair<Int, Int>) : Boolean {
            if(position.first < 0 || position.first > 10) return false;
            if(position.second < 0 || position.second > 10) return false;
            return true;
        }

        fun isGameOver(gameState: PublicGameState): Boolean {
            return false
        }

        fun getWinner(gameState: PublicGameState): UniqueIdentifier? {
            return null
        }
    }
}