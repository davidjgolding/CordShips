package com.cordships.webserver

import com.cordships.flows.IssuePublicGameFlow
import com.google.common.reflect.TypeToken
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import com.google.gson.Gson
import net.corda.core.messaging.startFlow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.reflect.Type

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
    private val player = "playerA"
    // players
    private val players: Set<String>? = setOf("playerA", "playerB", "playerC", "playerD", "playerE", "playerF")
    // grids
    private val grids = mutableMapOf<String, MutableList<MutableList<Int>>>(
            "playerA" to MutableList(n) { MutableList(n) { 0 } },
            "playerB" to MutableList(n) { MutableList(n) { 1 } },
            "playerC" to MutableList(n) { MutableList(n) { 1 } },
            "playerD" to MutableList(n) { MutableList(n) { 1 } },
            "playerE" to MutableList(n) { MutableList(n) { 1 } },
            "playerF" to MutableList(n) { MutableList(n) { 1 } })

    @CrossOrigin
    @PostMapping(value = ["/start"], produces = ["text/json"])
    private fun start(@RequestParam(value = "playerIDs") sPlayerIDs: String): ResponseEntity<String> {
        val gson = Gson()
        return try {
            val playersLst = gson.fromJson<List<String>>(sPlayerIDs, List :: class.java)
            val parties = playersLst.map { proxy.partiesFromName(it, true).first() }
            val x = proxy.startFlow(::IssuePublicGameFlow, parties)
            logger.info(x.id.toString())
            ResponseEntity("OK", HttpStatus.OK)
        } catch (ex: NoSuchElementException) {
            ResponseEntity("One of the players is invalid.", HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin
    @GetMapping(value = ["/connect"], produces = ["text/json"])
    private fun connect(): ResponseEntity<String> {
        return if (players != null) {
            val gson = Gson()
            val response = mapOf("players" to players)
            ResponseEntity(gson.toJson(response), HttpStatus.OK)
        } else {
            ResponseEntity("Not ready.", HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin
    @GetMapping(value = ["/id"], produces = ["text/json"])
    private fun id(): ResponseEntity<String> {
        val id: String? = proxy.nodeInfo().legalIdentities.first().name.organisation
        return if (id != null) {
            val gson = Gson()
            val response = mapOf("playerID" to id)
            ResponseEntity(gson.toJson(response), HttpStatus.OK)
        } else {
            ResponseEntity("Node name not found.", HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin
    @PostMapping(value = ["/grid"], produces = ["text/json"])
    private fun setGrid(@RequestParam(value = "grid") sGrid: String): ResponseEntity<String> {
        val gson = Gson()
        // create type for deserialization
        val type: Type = object : TypeToken<MutableList<MutableList<Int>>>() {}.type
        val grid = gson.fromJson<MutableList<MutableList<Int>>>(sGrid, type)
        return if (grid.size != n || grid[0].size != n) {
            // ensure grid is n x n and player exists
            ResponseEntity("Grid's dimensions aren't valid.", HttpStatus.NOT_FOUND)
        } else {
            // set the players grid
            grids[player] = grid
            return ResponseEntity("Grid set.", HttpStatus.OK)
        }
    }

    @CrossOrigin
    @GetMapping(value = ["/grid"], produces = ["text/json"])
    private fun getGrid(@RequestParam(value = "playerID") playerID: String): ResponseEntity<String> {
        val gson = Gson()
        // return the requested players grid if player exists
        return if (playerID in players!!) {
            val gson = Gson()
            val response = mapOf("size" to n, "grid" to grids[playerID])
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
        // ensure requested player exists
        if (player !in players!!)
            return ResponseEntity("Player not found.", HttpStatus.NOT_FOUND)
        // ensure you can't shoot yourself
        if (playerID == player)
            return ResponseEntity("You can't shoot yourself.", HttpStatus.BAD_REQUEST)
        // ensure coordinates are valid
        if (x !in 0..10 || y !in 0..10)
            return ResponseEntity("Coordinates are invalid.", HttpStatus.BAD_REQUEST)
        // ToDo: Lookup and set
        grids[playerID]!![x][y] = 3
        return ResponseEntity("Shot fired.", HttpStatus.OK)
    }
}