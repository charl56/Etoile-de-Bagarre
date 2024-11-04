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
import kotlinx.serialization.json.*

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

                // Cette fonction met a jour la liste des players, qui est appelé par le jeu ensuite
                // Pour mettre à jour l'affichage, on appelle le singleton WebSocket.getInstance().players
                "updatePlayersData" -> {
                    if(!_gameStarted.value) return
                    println("WEBSOCKET: Players data received : " + jsonObject["players"]?.jsonArray?.toString())
                    val players = jsonObject["players"]?.jsonArray?.map { it.jsonObject }

                    if(players != null) {
                        players.forEach { player ->
                            println("Player data: $player")
                            // Convert JSON en objet Kotlin, the execute updateOrAddPlayer,
                            // Don't update player if it's the current player
//                            updateOrAddPlayer(Player.fromMap(player))
                        }
                    }
                }
            }

        } catch (e: Exception) {
            println("WEBSOCKET: Error processing message: ${e.message}")
        }
    }

    // Function to send message to server, when player is in waiting room
    suspend fun joinAndWait(currentPlayer: Player) {
        val message = Json.encodeToString(mapOf(
            "type" to "joinWaitingRoom"
        ))
        session.send(Frame.Text(message))
    }

    // Function to send message to server, when player leave the waiting room
    suspend fun leaveRoom() {
        _gameStarted.value = false
        val message = Json.encodeToString(mapOf(
            "type" to "leaveWaitingRoom"
        ))
        session.send(Frame.Text(message))
    }


    /* ===================================================================================================
       Cette fonction doit être appelée à l'endroit où grâce au joystick, la position du joueur se déplace
       pour envoyer la position du joueur au serveur, a chaque fois tick/frame
       RESTE : envoyer le bon format de donnée dans "data"
       Enlever le sleep
       TODO : enlever ce commentaire une fois la fonction implémentée
       ================================================================================================== */
    // Send data player during game
    suspend fun updatePlayerData(player: Player?) {

        val player2 = Player("1", "ouai@mai.com", "pseudp", 3, 34f, 421f, 90, true);
//        println("json encodeToString " + Json.encodeToString(player2));
//        println("json encodeToString " + Json.encodeToString(Player.serializer(), player2))



        // Remove while, set up for tests
        while (System.currentTimeMillis() - System.currentTimeMillis() < 10_000 && _gameStarted.value) {
//            println("send data")
//            val message = Json.encodeToString(mapOf(
//                "type" to "updatePlayerData",
//                "data" to player2
//            ))
//            session.send(Frame.Text(message))

            // Remove thread sleep, set up for tests
            Thread.sleep(100)

        }

    }

    // Function to update a player in the list, when we get data of alll players from server
    fun updateOrAddPlayer(newPlayer: Player) {
        _players.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.id == newPlayer.id }
            if (existingIndex != -1) {
                // Update existing object
                currentList.toMutableList().apply {
                    this[existingIndex] = newPlayer
                }
            } else {
                // Add new object
                currentList + newPlayer
            }
        }
    }


    suspend fun leaveWebSocket() {
        session.close()
        client.close()
    }
}