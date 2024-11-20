package fr.eseo.ld.android.cp.nomdujeu.service

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import io.ktor.client.engine.cio.*
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

import kotlinx.coroutines.flow.*
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.url
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

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


    private val client = HttpClient(CIO) {
        install(WebSockets)
        engine {
            https {
                // Accept all ssl certificates
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            }
        }
    }

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _playerCount = MutableStateFlow(0)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    var session: DefaultClientWebSocketSession? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

//    var wsUrl = "ws://10.0.2.2:5025/"      // Local emulator android
//    var wsUrl = "wss://51.254.119.241/ws-edb/"     // Server IP
    var wsUrl = "wss://charles.studi0426.com/ws-edb/"


    // TODO refactore. Var set in ProcessEndGame, et are here to be get by endGameScreen, to show end message
    var winner = ""
    var kills = ""



    // TODO : refactore if posible. Add viewModel to websocket to can use function for BDD, endGame
    private lateinit var playerViewModel: PlayerViewModel;
    private lateinit var gameViewModel: GameViewModel;

    fun InitViewModels(playerViewModel: PlayerViewModel, gameViewModel: GameViewModel){
        this.playerViewModel = playerViewModel;
        this.gameViewModel = gameViewModel;
    }



    fun setPlayer(player: Player) {
        _player.value = player
    }

    suspend fun checkAvailability(): Boolean {
        return try {
            withTimeout(5000) { // 5s of timeout
                session = client.webSocketSession {
                    url(wsUrl)
                }

                // Function who will get messages from server
                coroutineScope.launch { receiveMessages() }
            }
            true
        } catch (e: Exception) {
            when (e) {
                is ConnectTimeoutException -> println("WEBSOCKET: Connection timed out")
                is SSLHandshakeException -> println("WEBSOCKET: SSL handshake failed. Check your certificate.")
                is UnknownHostException -> println("WEBSOCKET: Unknown host. Check your URL.")
                else -> println("WEBSOCKET: Connection failed: ${e.javaClass.simpleName} - ${e.message}")
            }
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
                "playerCount" -> {
                    _playerCount.value = jsonObject["count"]?.jsonPrimitive?.int ?: 0  // Update nb of player in waiting room
                    _player.value?.let {
                        _player.value = it.copy(
                            x = jsonObject["spawnPositionX"]?.jsonPrimitive?.float ?: 0.00f,
                            y = jsonObject["spawnPositionY"]?.jsonPrimitive?.float ?: 0.00f
                        )
                    }
                }
                "gameStart" -> {
                    _gameStarted.value = true
                }
                "updatePlayersData" -> {
                    if (!_gameStarted.value) return
                    processPlayersUpdate(jsonObject)
                }
                "isDead" -> {
                    processIsDead(jsonObject)
                }
                "endGame" -> {
                    processEndGame(jsonObject)
                }
            }

        } catch (e: Exception) {
            println("WEBSOCKET: Error processing message: ${e.message}")
        }
    }

    // Function to send message to server, when player is in waiting room
    suspend fun joinAndWait(currentPlayer: Player, selectedPlayerCount: Int) {
        println("WEBSOCKET: Joining waiting room $selectedPlayerCount")
        val message = Json.encodeToString(mapOf(
            "type" to "joinWaitingRoom",
            "playerId" to currentPlayer.id,
            "roomSize" to selectedPlayerCount.toString()
        ))
        sendMessage(message);
    }

    // Function to send message to server, when player leave the waiting room
    suspend fun leaveRoom() {
        _gameStarted.value = false
        val message = Json.encodeToString(mapOf(
            "type" to "leaveWaitingRoom"
        ))
        sendMessage(message)
    }



    // Function to convert data receive list of players, in koltin obj for list in client side
    fun processPlayersUpdate(jsonObject: JsonObject ) {
        val newPlayers = jsonObject["players"]?.jsonArray?.mapNotNull { playerJson ->

            val player = playerJson.jsonObject
            val id = player["id"]?.jsonPrimitive?.content ?: return@mapNotNull null

            // If it's actual player, update only life and kills
            if (id == _player.value?.id) {
                _player.value = _player.value?.copy(
                    life = player["life"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    kills = player["kills"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0)
                // TODO : AJouter une animation au changement de vie ?
                // Check if is alive
                if(player["isAlive"]?.jsonPrimitive?.content?.toBoolean() == true) {
                    // TODO : delete entity when player is dead ?
                    // TODO : change game mode to spectate ?
                }
                return@mapNotNull null
            }
            // Else update
            Player(
                id = id,
                x = player["x"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                y = player["y"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                life = player["life"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                isAlive = player["isAlive"]?.jsonPrimitive?.content?.toBoolean() ?: false,
            )
            // TODO : if this enemy player is dead, remove entity from world

        } ?: emptyList()

        newPlayers.forEach { updateOrAddPlayers(it) }
    }

    // Function to update a player in the list, when we get data of all players from server
    fun updateOrAddPlayers(newPlayer: Player) {


        _players.value = _players.value.toMutableList().apply {
            val existingIndex = indexOfFirst { it.id == newPlayer.id }
            if (existingIndex != -1) {
                this[existingIndex] = newPlayer
            } else {
                add(newPlayer)
            }
        }
    }



    // Send position player during game, call by MoveSystem, where new player position is set each tick
    fun updatePlayerData(x: Float, y: Float) {
        _player.value = _player.value?.copy(x = x, y = y)
        player?.let{ p ->
            val encodedPlayer = Json.encodeToString(mapOf(
                "id" to _player.value?.id,
                "pseudo" to _player.value?.pseudo,
                "x" to _player.value?.x.toString(),
                "y" to _player.value?.y.toString(),
                "kills" to _player.value?.kills.toString(),
                "life" to _player.value?.life.toString(),
                "isAlive" to _player.value?.isAlive.toString(),
            ))
            val message = Json.encodeToString(mapOf(
                "type" to "updatePlayerData",
                "data" to encodedPlayer
            ))
            coroutineScope.launch {
                sendMessage(message)
            }
        }
    }

    // TODO : call this function when we attack, and detect enemy collision
    suspend fun onHitEnemy(victimId: Int, shooterId: Int, damage: Int){
        val data = Json.encodeToString(mapOf(
            "victimId" to victimId,
            "shooterId" to shooterId,
            "damage" to damage
        ))

        val message = Json.encodeToString(mapOf(
            "type" to "onHit",
            "data" to data
        ))

        sendMessage(message);
    }


    // Is use to know when our entity need pour be remove from the game
    fun processIsDead(jsonObject: JsonObject){
        val shooterId = jsonObject["shooterId"]?.jsonPrimitive?.content ?: ""
        val victimId = jsonObject["victimId"]?.jsonPrimitive?.content ?: ""

        if(victimId == _player.value?.id){
            println("t mort")
            // TODO : remove entity from game
        }
        // Don't need to update shooter kills, it's done in backend

    }

    // When the game is finish, server send message et execute this
    fun processEndGame(jsonObject: JsonObject) {
        winner = jsonObject["winner"]?.jsonPrimitive?.content ?: ""
        kills = jsonObject["kills"]?.jsonPrimitive?.content ?: ""
        var id = jsonObject["id"]?.jsonPrimitive?.content ?: ""

        if (id == _player.value?.id) {
            playerViewModel.addWinToPlayerWithId(id);
        }

        gameViewModel.endGame()
    }

    suspend fun sendMessage(message: String) {
        session?.send(Frame.Text(message))
    }


    suspend fun leaveWebSocket() {
        session?.close()
        client.close()
    }
}