package fr.eseo.ld.android.cp.nomdujeu.service

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.repository.FirestoreRepository
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
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

    var session: DefaultClientWebSocketSession? = null
    val incomingMessages: Channel<String> = Channel()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    //    var wsUrl = "10.0.2.2:5025"      // Local emulator android
    var wsUrl = "172.24.1.37/ws-edb/"     // Server IP


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
                    url("ws://${wsUrl}/")
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
                "playerCount" -> {
                    _playerCount.value = jsonObject["count"]?.jsonPrimitive?.int ?: 0  // Update nb of player in waiting room
                    _player.value?.let { _player.value = it.copy(listPosition = jsonObject["listPosition"]?.jsonPrimitive?.int ?: 0) }  // Update index of position in the list
                }
                "gameStart" -> {
                    _gameStarted.value = true
                    coroutineScope.launch {
                        updatePlayerData(null)
                    }
                }
                // TODO : Pour récupérer les données. Deopuis le jeu on appelle : WebSocket.getInstance().players, à chaque tick
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
    suspend fun joinAndWait(currentPlayer: Player) {
        val message = Json.encodeToString(mapOf(
            "type" to "joinWaitingRoom"
        ))
        println(message);
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

            Player(
                id = id,
                pseudo = player["pseudo"]?.jsonPrimitive?.content ?: "",
                x = player["x"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                y = player["y"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f,
                life = player["life"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                isAlive = player["isAlive"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                listPosition = player["listPosition"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            )

            // TODO : if players are dead, remove entity from game

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
//                "kills" to p.kills.toString(),
//                "life" to p.life.toString(),
//                "isAlive" to p.isAlive.toString(),
//                "listPosition" to p.listPosition.toString()
//            ))
//            val message = Json.encodeToString(mapOf(
//                "type" to "updatePlayerData",
//                "data" to encodedPlayer
//            ))
//            coroutineScope.launch {
//                sendMessage(message)
//            }
//        }

        // TODO : remove this and while function, when function will be call by game loop
        val encodedPlayer = Json.encodeToString(mapOf(
            "id" to _player.value?.id,
            "pseudo" to _player.value?.pseudo,
            "x" to _player.value?.x.toString(),
            "y" to _player.value?.y.toString(),
            "kills" to _player.value?.kills.toString(),
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
            sendMessage(message);

            // Remove thread sleep, set up for tests
            Thread.sleep(100)

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