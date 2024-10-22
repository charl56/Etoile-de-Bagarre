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

class WebSocket {
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

    suspend fun checkAvailability(): Boolean {
        return try {
            withTimeout(5000) { // 5 secondes de timeout
                client.webSocketSession {
                    url("ws://localhost:5025/waiting-room")
                }.close()
            }
            true
        } catch (e: Exception) {
            println("Le serveur WebSocket n'est pas disponible: ${e.message}")
            false
        }
    }

    suspend fun joinAndWait(currentPlayer: Player) {
        try {
            session = client.webSocketSession {
                url("ws://localhost:5025/waiting-room")
            }

            // Envoyer les informations du joueur au serveur
            sendPlayerData(currentPlayer)

            // Lancer la réception des messages
            coroutineScope.launch { receiveMessages() }

            // Traiter les messages entrants
            for (message in incomingMessages) {
                val data = Json.decodeFromString<Map<String, Any>>(message)
                println("message: $message")
                when (data["type"]) {
                    "playerCount" -> _playerCount.value = (data["count"] as Double).toInt()
                    "playerList" -> updatePlayerList(data["players"] as List<Map<String, Any>>)
                    "gameStart" -> _gameStarted.value = true
                }
            }
        } catch (e: Exception) {
            println("Erreur lors de la connexion à la salle d'attente: ${e.message}")
        }
    }

    private suspend fun receiveMessages() {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    incomingMessages.send(frame.readText())
                }
            }
        } catch (e: Exception) {
            println("Erreur lors de la réception des messages: ${e.message}")
        }
    }

    suspend fun sendPlayerData(player: Player) {
        val message = Json.encodeToString(mapOf(
            "type" to "playerInfo",
            "player" to player
        ))
        session.send(Frame.Text(message))
    }

    suspend fun getPlayersData(): List<Player> {
        return _players.value
    }

    private fun updatePlayerList(playerData: List<Map<String, Any>>) {
        _players.value = playerData.mapNotNull { playerMap ->
            try {
                Player.fromMap(playerMap)
            } catch (e: Exception) {
                println("Erreur lors de la conversion des données du joueur: ${e.message}")
                null
            }
        }
    }

    suspend fun leaveRoom() {
        session.close()
        client.close()
    }
}