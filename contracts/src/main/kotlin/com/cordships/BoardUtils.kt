package com.cordships

import com.cordships.states.HitOrMiss

typealias Board = Array<Array<HitOrMiss>>

class BoardUtils {
    companion object {

        fun checkIfValidPositions(position: Pair<Int, Int>) = checkIfValidPosition(position)

        private fun checkIfValidPosition(position: Pair<Int, Int>) : Boolean {
            if(position.first < 0 || position.first >= 10) return false;
            if(position.second < 0 || position.second >= 10) return false;
            return true;
        }
    }
}