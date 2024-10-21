package fr.eseo.ld.android.cp.nomdujeu.service

import com.google.firebase.database.*
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
//    private var playerCount = 0
    private val _playerCount = MutableStateFlow(0)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    suspend fun joinAndWait(): Boolean {
        try {
            // Add user to wait list
            playerRef = addPlayerToRoom()

            // Wait for 5 players to join
            return waitForPlayers()
        } catch (e: Exception) {
            println("Erreur when connexion to waiting room: ${e.message}")
            return false
        }
    }

    private suspend fun addPlayerToRoom(): DatabaseReference {
        val newPlayerRef = roomRef.push()
        newPlayerRef.setValue(true).await()
        return newPlayerRef
    }

    private suspend fun waitForPlayers(): Boolean = suspendCancellableCoroutine { continuation ->
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                _playerCount.value = count
                if (count >= 5) {
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