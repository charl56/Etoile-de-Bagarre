package fr.eseo.ld.android.cp.nomdujeu.service

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import io.ktor.client.request.url
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebSocket2 {
    private val client = HttpClient {
        install(WebSockets)
    }
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _playerCount = MutableStateFlow(0)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private lateinit var session: DefaultClientWebSocketSession
    private val incomingMessages = Channel<String>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var wsUrl = "10.0.2.2:5025"      // Local emulator android
//    var wsUrl = "172.23.1.4:5025"     // Local computer

    suspend fun checkAvailability(): Boolean {
        return try {
            withTimeout(5000) { // 5s of timeout
                client.webSocketSession {
                    url("ws://${wsUrl}/waiting-room")
                }.close()
            }
            true
        } catch (e: Exception) {
            println("WEBSOCKET : Websocket server not available: ${e.message}")
            false
        }
    }

    suspend fun joinAndWait(currentPlayer: Player) {
        try {
            session = client.webSocketSession {
                url("ws://localhost:5025/waiting-room")
            }

            // Send data player to the server
            sendPlayerData(currentPlayer)

            // Start receiving messages
            coroutineScope.launch { receiveMessages() }

            // Input messages
            for (message in incomingMessages) {
                val data = Json.decodeFromString<Map<String, Any>>(message)
                println("WEBSOCKET : incomming message: $message")
                when (data["type"]) {
                    "playerCount" -> _playerCount.value = (data["count"] as Double).toInt()
                    "playerList" -> updatePlayerList(data["players"] as List<Map<String, Any>>)
                    "gameStart" -> _gameStarted.value = true
                }
            }
        } catch (e: Exception) {
            println("WEBSOCKET : error connection to waiting room: ${e.message}")
        }
    }

    // Messages from websocket
    private suspend fun receiveMessages() {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    incomingMessages.send(frame.readText())
                }
            }
        } catch (e: Exception) {
            println("WEBSOCKET : Error when getting messages: ${e.message}")
        }
    }

    // Send data player during game
    suspend fun sendPlayerData(player: Player) {
        val message = Json.encodeToString(mapOf(
            "type" to "playerInfo",
            "player" to player
        ))
        session.send(Frame.Text(message))
    }

    // Get players data during game
    suspend fun getPlayersData(): List<Player> {
        return _players.value
    }


    private fun updatePlayerList(playerData: List<Map<String, Any>>) {
        _players.value = playerData.mapNotNull { playerMap ->
            try {
                Player.fromMap(playerMap)
            } catch (e: Exception) {
                println("WEBSOCKET : Error when trying to convert data: ${e.message}")
                null
            }
        }
    }

    suspend fun leaveRoom() {
        session.close()
        client.close()
    }
}