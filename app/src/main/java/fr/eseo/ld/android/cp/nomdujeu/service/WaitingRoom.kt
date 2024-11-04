package fr.eseo.ld.android.cp.nomdujeu.service

import com.google.firebase.database.*
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WaitingRoom(private val database: FirebaseDatabase) {

    private val roomRef = database.getReference("waiting_room")
    private lateinit var playerRef: DatabaseReference
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    suspend fun joinAndWait(currentPlayer: Player): Boolean {
        try {
            playerRef = addPlayerToRoom(currentPlayer)
            return waitForPlayers()
        } catch (e: Exception) {
            println("Erreur lors de la connexion à la salle d'attente: ${e.message}")
            return false
        }
    }

    private suspend fun addPlayerToRoom(player: Player): DatabaseReference {
        val newPlayerRef = roomRef.push()
        newPlayerRef.setValue(player).await()
        return newPlayerRef
    }

    private suspend fun waitForPlayers(): Boolean = suspendCancellableCoroutine { continuation ->
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersList = snapshot.children.mapNotNull {
                    try {
                        it.getValue(Player::class.java)
                    } catch (e: DatabaseException) {
                        println("Données incorrectes détectées : ${e.message}")
                        null // Ignorer les entrées invalides
                    }
                }
                _players.value = playersList
                if (playersList.size >= 5) {
                    roomRef.removeEventListener(this)
                    continuation.resume(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        }

        roomRef.addValueEventListener(valueEventListener)

        continuation.invokeOnCancellation {
            roomRef.removeEventListener(valueEventListener)
        }
    }

    fun leaveRoom() {
        playerRef.removeValue()
    }
}