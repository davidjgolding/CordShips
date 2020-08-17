package com.cordships.states

/**
 * A ship representing the placement of a ship piece on a private game board state
 *
 * @param descriptor The direction in which the boat is facing
 * @param shipSize The length of the ship
 */
class Ship(descriptor: String, shipSize: ShipSize) {

    /** A list of coordinates representing how a ship has been placed */
    lateinit var coordinates: List<Pair<Int, Int>>

    /** Vessel class */
    val vesselClass = shipSize.name

    /** Initializes the coordinates with the provided descriptor */
    init {
        // Descriptor format 'A0E', 'A' -> x axis, '0' -> y axis, 'E' -> direction indicator
        val mutablePoints = mutableListOf<Pair<Int, Int>>()
        val x = descriptor.toCharArray()[0].toInt() % 65
        val y = descriptor.toCharArray()[1].toString().toInt() //// RETURN TO TO GET LARGER NUMBERS e.g. 10
        val direction = descriptor.toCharArray()[2]
        val startingPoint = Pair(x, y)
        mutablePoints.add(startingPoint)

        if (shipSize.length > 1) {
            val xDirection: Int
            val yDirection: Int
            when (direction) {
                'E' -> {
                    xDirection = 1
                    yDirection = 0
                }
                'W' -> {
                    xDirection = -1
                    yDirection = 0
                }
                'N' -> {
                    xDirection = 0
                    yDirection = -1
                }
                'S' -> {
                    xDirection = 0
                    yDirection = 1
                }
                else -> {
                    xDirection = 0
                    yDirection = 0
                }
            }
            for (i in 1 until shipSize.length) {
                mutablePoints.add(Pair(startingPoint.first + (i * xDirection),
                        startingPoint.second + (i * yDirection)))
            }

            coordinates = mutablePoints.toList()
        }
    }

    /** A class which encapsulates all ships with various sizes */
    enum class ShipSize(val length: Int) {
        AirCraftCarrier(5),
        BattleShip(4),
        Cruiser(3),
        Destroyer(2),
        Submarine(2)
    }
}