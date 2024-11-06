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
    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _playerCount = MutableStateFlow(0)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null
    private val incomingMessages = Channel<String>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var wsUrl = "10.0.2.2:5025"      // Local emulator android
//    var wsUrl = "172.23.1.4:5025"     // Server IP

    fun setPlayer(player: Player) {
        _player.value = player
    }

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
            session?.incoming?.consumeEach { frame ->
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
            val jsonObject = Json.parseToJsonElement(message).jsonObject


            when (jsonObject["type"]?.jsonPrimitive?.content) {
                // Update player count, display in home screen
                "playerCount" ->
                {
                    _playerCount.value = jsonObject["count"]?.jsonPrimitive?.int ?: 0;  // Update nb of player in waiting room
                    _player.value?.let {                                                // Update index of position in the list
                        _player.value = it.copy(listPosition = jsonObject["listPosition"]?.jsonPrimitive?.int ?: 0)
                    }
                    println("WEBSOCKET: Player listPosition updated: ${_player.value?.listPosition}")
                }
                "gameStart" -> {
                    _gameStarted.value = true
                    coroutineScope.launch {
                        updatePlayerData(null)
                    }
                }
                // This function update data of players, in players lsit
                // TODO : Pour récupérer les données, on appelle : WebSocket.getInstance().players, à chaque tick
                "updatePlayersData" -> processPlayersUpdate(jsonObject)
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
        session?.send(Frame.Text(message))
    }

    // Function to convert data receive list of players, in koltin obj for list in client side
    private fun processPlayersUpdate(jsonObject: JsonObject) {
        if (!_gameStarted.value) return

        val newPlayers = jsonObject["players"]?.jsonArray?.mapNotNull { playerJson ->
            val player = playerJson.jsonObject
            val id = player["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            if (id == _player.value?.id) return@mapNotNull null

            Player(
                id = id,
                pseudo = player["pseudo"]?.jsonPrimitive?.content ?: "",
                x = player["x"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                y = player["y"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                life = player["life"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                isAlive = player["isAlive"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                listPosition = player["listPosition"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            )
        } ?: emptyList()

        newPlayers.forEach { updateOrAddPlayer(it) }
    }

    // Function to update a player in the list, when we get data of all players from server
    fun updateOrAddPlayer(newPlayer: Player) {
        _players.value = _players.value.toMutableList().apply {
            val existingIndex = indexOfFirst { it.id == newPlayer.id }
            if (existingIndex != -1) {
                this[existingIndex] = newPlayer
            } else {
                add(newPlayer)
            }
        }
    }


    /* ===================================================================================================
       Cette fonction doit être appelée à l'endroit où grâce au joystick, la position du joueur se déplace
       pour envoyer la position du joueur au serveur, a chaque fois tick/frame
        TODO : Replacer _player par le parametres player, une fois appelée depuis le joystick
        TODO : Ensuite enlever le while, on garde juste la création du message et l'envoie
        TODO : On pourra enlever le suspend aussi
       TODO : Enlever ce commentaire une fois la fonction implémentée
       ================================================================================================== */
    // Send data player during game
    suspend fun updatePlayerData(player: Player?) {

// TODO : use this when function will be call by game loop
//        player?.let{ p ->
//            val encodedPlayer = Json.encodeToString(mapOf(
//                "id" to p.id,
//                "pseudo" to p.pseudo,
//                "x" to p.x.toString(),
//                "y" to p.y.toString(),
//                "life" to p.life.toString(),
//                "isAlive" to p.isAlive.toString(),
//                "listPosition" to p.listPosition.toString()
//            ))
//            val message = Json.encodeToString(mapOf(
//                "type" to "updatePlayerData",
//                "data" to encodedPlayer
//            ))
//            coroutineScope.launch {
//                session?.send(Frame.Text(message))
//            }
//        }

        // TODO : remove this and while function, when function will be call by game loop
        val encodedPlayer = Json.encodeToString(mapOf(
            "id" to _player.value?.id,
            "pseudo" to _player.value?.pseudo,
            "x" to _player.value?.x.toString(),
            "y" to _player.value?.y.toString(),
            "life" to _player.value?.life.toString(),
            "isAlive" to _player.value?.isAlive.toString(),
            "listPosition" to _player.value?.listPosition.toString()
        ))

        // Remove while, set up for tests
        while (System.currentTimeMillis() - System.currentTimeMillis() < 10_000 && _gameStarted.value) {
            val message = Json.encodeToString(mapOf(
                "type" to "updatePlayerData",
                "data" to encodedPlayer
            ))
            session?.send(Frame.Text(message))

            // Remove thread sleep, set up for tests
            Thread.sleep(100)

        }

    }


    // Function to send message to server, when player leave the waiting room
    suspend fun leaveRoom() {
        _gameStarted.value = false
        val message = Json.encodeToString(mapOf(
            "type" to "leaveWaitingRoom"
        ))
        session?.send(Frame.Text(message))
    }


    suspend fun leaveWebSocket() {
        session?.close()
        client.close()
    }
}