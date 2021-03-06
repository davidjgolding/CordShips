package com.cordships.webserver

import com.cordships.flows.*
import com.google.common.reflect.TypeToken
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import com.google.gson.Gson
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.FlowHandle
import net.corda.core.messaging.startFlow
import org.apache.commons.lang3.RandomUtils.nextInt
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.reflect.Type
import java.util.*
import kotlin.NoSuchElementException
import org.apache.commons.lang3.RandomUtils.nextInt
import com.cordships.states.Ship.ShipSize.*
import com.cordships.states.HitOrMiss
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.serialization.CordaSerializable
import java.util.Random
import kotlin.collections.HashMap

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    // size of grid
    private val n = 10
    // current player
    private val player = proxy.nodeInfo().legalIdentities.first().name.organisation
    // players
    private var players: Set<String>? = null//= setOf("playerA", "playerB", "playerC", "playerD", "playerE", "playerF")
    // grids
    private var playerBoard = MutableList(n) { MutableList(n) { 0 } }
    private var allBoards: MutableMap<String, List<List<Int>>> = HashMap()

    val shipsUsed = listOf(AirCraftCarrier, BattleShip, Cruiser, Destroyer, Destroyer, Submarine, Submarine)
    private var gameID: UniqueIdentifier? = null

    private var gameStarted = false

    @CordaSerializable
    enum class Directions(val coord: Pair<Int, Int>) {
        N(Pair(0, -1)),
        S(Pair(0, 1)),
        E(Pair(1, 0)),
        W(Pair(-1, 0))
    }
    private fun <E> Array<E>.random(): E? = if (size > 0) get(Random().nextInt(size)) else null

    private fun randomPoint(direction: Int, shipSize: Int): Int {
        return when (direction) {
            1 -> nextInt(0, n - shipSize)
            -1 -> nextInt(shipSize, n)
            else -> nextInt(0, n)
        }
    }

    private fun randomStart() {
        if (gameID == null) {
            val playerGameState = proxy.vaultQuery(com.cordships.states.PublicGameState :: class.java).states
            gameID = playerGameState.last().state.data.linearId
        }

        var startPositions = shipsUsed.map {
            val direction = Directions.values().random()!!
            var rand = randomPoint(direction.coord.first, it.length)
            val x = (rand + 65).toChar()
            val y = randomPoint(direction.coord.second, it.length)
            logger.info("($rand,$y): $x$y$direction" )
            "$x$y$direction"
        }
        logger.info(startPositions.toString())
        proxy.startFlow(::PiecePlacementFlow, gameID!!, startPositions).returnValue.then {
            val playerGameState = proxy.vaultQuery(com.cordships.states.PrivateGameState :: class.java).states
            val board = playerGameState.last().state.data.board
            logger.info(board.map{it.coordinates}.toString())
            board.forEach { ship -> ship.coordinates.forEach { (x, y) -> playerBoard[x][y] = 2 } }
        }
    }

    @CrossOrigin
    @PostMapping(value = ["/start"], produces = ["text/json"])
    private fun start(@RequestParam(value = "playerIDs") sPlayerIDs: String): ResponseEntity<String> {
        val gson = Gson()
        return try {
            // try to start a new game with the player IDs provided
            val playersLst = gson.fromJson<List<String>>(sPlayerIDs, List :: class.java)
            val parties = playersLst.map { proxy.partiesFromName(it, true).first() }
            val game = proxy.startFlow(::IssuePublicGameFlow, parties)
            gameID = game.returnValue.get().linearId
            randomStart()
            players = playersLst.toSet()
            val gson = Gson()
            val response = mapOf("gameID" to gameID!!.id.toString())
            ResponseEntity(gson.toJson(response), HttpStatus.OK)
        } catch (ex: NoSuchElementException) {
            ResponseEntity("One of the players is invalid.", HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin
    @GetMapping(value = ["/random"], produces = ["text/json"])
    private fun random(): ResponseEntity<String> {
        val gson = Gson()
        var startPositions = shipsUsed.map {
            val direction = Directions.values().random()!!
            var rand = randomPoint(direction.coord.first, it.length)
            val x = (rand + 65).toChar()
            val y = randomPoint(direction.coord.second, it.length)
            logger.info("($rand,$y): $x$y$direction" )
            "$x$y$direction"
        }
       return ResponseEntity(gson.toJson(startPositions), HttpStatus.OK)
    }


    @CrossOrigin
    @GetMapping(value = ["/connect"], produces = ["text/json"])
    private fun connect(): ResponseEntity<String> {
        updateGrids()
        val gson = Gson()
        val response = mapOf("players" to players)
        return ResponseEntity(gson.toJson(response), HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping(value = ["/id"], produces = ["text/json"])
    private fun id(): ResponseEntity<String> {
        return if (player != null) {
            val gson = Gson()
            val response = mapOf("playerID" to player)
            ResponseEntity(gson.toJson(response), HttpStatus.OK)
        } else {
            ResponseEntity("Node name not found.", HttpStatus.NOT_FOUND)
        }
    }

    private fun updateGrids() {
        val playerGameState = proxy.vaultQuery(com.cordships.states.PublicGameState :: class.java).states
        playerGameState.last().state.data.playerBoards.forEach { player ->
            allBoards!![player.key.name.organisation] = player.value.map { row -> row.map{ it.num } }
        }
        players = allBoards?.keys?.toSet()
        gameID = playerGameState.last().state.data.linearId
    }

    @CrossOrigin
    @GetMapping(value = ["/competitorsGrids"], produces = ["text/json"])
    private fun getCompetitorsGrids(): ResponseEntity<String> {
        updateGrids()
        val competitors = players?.minus(player)!!
        val gson = Gson()
        val response = competitors.map{ it to allBoards[it] }.toMap()
        return ResponseEntity(gson.toJson(response), HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping(value = ["/grids"], produces = ["text/json"])
    private fun getGrid(@RequestParam(value = "playerID") playerID: String): ResponseEntity<String> {
        updateGrids()
        val gson = Gson()
        // return the requested players grid if player exists
        return if (playerID in players!!) {
            val gson = Gson()
            val response = if(playerID == player) {
                val playerGameState = proxy.vaultQuery(com.cordships.states.PrivateGameState :: class.java)
                val board = playerGameState.states.last().state.data.board
                board.forEach { ship -> ship.coordinates.forEach { (x, y) -> playerBoard[x][y] = 2 } }
                val grid = playerBoard.mapIndexed {x, row -> row.mapIndexed {y, cell ->
                    if (allBoards[player]!![x][y] != 1) {
                        allBoards[player]!![x][y]
                    } else {
                        cell
                    }
                }}
                mapOf("size" to n, "grid" to grid)
            } else {
                mapOf("size" to n, "grid" to allBoards[playerID])
            }
            ResponseEntity(gson.toJson(response), HttpStatus.OK)
        } else {
            ResponseEntity("Player not found.", HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin
    @PostMapping(value = ["/shoot"], produces = ["text/json"])
    private fun shoot(@RequestParam(value = "playerID") playerID: String,
                      @RequestParam(value = "x") x: Int,
                      @RequestParam(value = "y") y: Int): ResponseEntity<String> {
        if (gameID == null) {
            updateGrids()
        }
        // ensure requested player exists
        if (playerID !in players!!)
            return ResponseEntity("Player not found.", HttpStatus.NOT_FOUND)
        // ensure you can't shoot yourself
        if (playerID == player)
            return ResponseEntity("You can't shoot yourself.", HttpStatus.BAD_REQUEST)
        // ensure coordinates are valid
        if (x !in 0..n || y !in 0..n)
            return ResponseEntity("Coordinates are invalid.", HttpStatus.BAD_REQUEST)
        val playerParty = proxy.partiesFromName(playerID, true).first()
        proxy.startFlow(::AttackFlow, listOf(Shot(Pair(x, y), playerParty)), gameID!!)
        return ResponseEntity("Shot fired.", HttpStatus.OK)
    }

    @CrossOrigin
    @GetMapping(value = ["/turn"], produces = ["text/json"])
    private fun turn(): ResponseEntity<String> {
        val playerGameState = proxy.vaultQuery(com.cordships.states.PublicGameState :: class.java).states
        val playerTurn = players!!.toList()[playerGameState.last().state.data.partyTurn]
        val winner: String? = playerGameState.last().state.data.getWinner()?.name?.organisation
        val response = mapOf("turn" to playerTurn, "winner" to (winner ?: "null"))
        val gson = Gson()
        return ResponseEntity(gson.toJson(response), HttpStatus.OK)

    }
}