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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WebSocket private constructor() {


    companion object {
        @Volatile
        private var INSTANCE: WebSocket? = null

        fun getInstance(): WebSocket {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocket().also { INSTANCE = it }
            }
        }
    }


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
//    var wsUrl = "172.23.1.4:5025"     // Server IP

    suspend fun checkAvailability(): Boolean {
        return try {
            withTimeout(5000) { // 5s of timeout
                session = client.webSocketSession {
                    url("ws://${wsUrl}/waiting-room")
                }

                // Function who will get messages from server
                coroutineScope.launch { receiveMessages() }
            }
            true
        } catch (e: Exception) {
            println("WEBSOCKET : Websocket server not available: ${e.message}")
            false
        }
    }


    // Messages from websocket
    private suspend fun receiveMessages() {
        try {
            session.incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    processMessage(message)
                }
            }
        } catch (e: Exception) {
            println("WEBSOCKET: Error while receiving messages: ${e.message}")
        }
    }

    private fun processMessage(message: String) {
        try {
            val jsonElement = Json.parseToJsonElement(message)
            val jsonObject = jsonElement.jsonObject

            when (jsonObject["type"]?.jsonPrimitive?.content) {
                "playerCount" -> {
                    val count = jsonObject["count"]?.jsonPrimitive?.int
                    if (count != null) {
                        _playerCount.value = count
                        println("WEBSOCKET: Player count updated to $count")
                    }
                }
                "gameStart" -> {
                    _gameStarted.value = true
                    println("WEBSOCKET: Game started")
                    coroutineScope.launch {
                        updatePlayerData(null)
                    }
                }
                "updatePlayersData" -> {
                    if(!_gameStarted.value) return
                    println("WEBSOCKET: Players data received : " + jsonObject["players"]?.jsonArray?.toString())
                    val players = jsonObject["players"]?.jsonArray?.map { it.jsonObject }

                    if(players != null) {
                        players.forEach { player ->
                            println("Player data: $player")
                            // Ici, tu peux convertir chaque élément JSON en objet Kotlin:
                        }
                    }
//                    updatePlayersData(players)
                }
            }

        } catch (e: Exception) {
            println("WEBSOCKET: Error processing message: ${e.message}")
        }
    }


    suspend fun joinAndWait(currentPlayer: Player) {
        val message = Json.encodeToString(mapOf(
            "type" to "joinWaitingRoom"
        ))
        session.send(Frame.Text(message))
    }

    suspend fun leaveRoom() {
        _gameStarted.value = false
        val message = Json.encodeToString(mapOf(
            "type" to "leaveWaitingRoom"
        ))
        session.send(Frame.Text(message))
    }


    /* ===================================================================================================
       Cette fonction doit être appelée à l'endroit où grâce au joystick, la position du joueur se déplace
       pour envoyer la position du joueur au serveur
       RESTE : envoyer le bon format de donnée dans "data"
       TODO : enlever ce commentaire une fois la fonction implémentée
       ================================================================================================== */
    // Send data player during game
    suspend fun updatePlayerData(player: Player?) {

        val player2 = Player("1", "ouai@mai.com", "pseudp", 3, 34f, 421f, 90, true);

        while (System.currentTimeMillis() - System.currentTimeMillis() < 10_000 && _gameStarted.value) {
            println("send data")
            val message = Json.encodeToString(mapOf(
                "type" to "updatePlayerData",
                "data" to "player2"
            ))
            session.send(Frame.Text(message))

            Thread.sleep(100)

        }

    }

    /* ==========================================================================================
       Cette fonction doit être appelée à l'endroit où sont stockées les entités des enemies.
       On récupère leurs positions sur le serveur, et on les assignes aux entités correspondantes
       TODO : enlever ce commentaire une fois la fonction implémentée
       RESTE : récupérer une liste de données, sous le bon format pour assigner direct dans le jeu
       ========================================================================================= */
    // Get players data during game
    private fun updatePlayersData(players: Map<String, JsonElement>) {
        // TODO : update player list
        println("WEBSOCKET: Players list updated to $players")
    }



    suspend fun leaveWebSocket() {
        session.close()
        client.close()
    }
}