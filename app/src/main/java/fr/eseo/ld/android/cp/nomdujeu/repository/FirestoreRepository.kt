package fr.eseo.ld.android.cp.nomdujeu.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    private val playersCollection = firestore.collection("players")

    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()



    fun addPlayer(player : Player) {
        if (player.id.isEmpty()) {
            player.id = playersCollection.document().id
        }
        playersCollection.document(player.id).set(player)
    }

    fun getPlayers(callback : (List<Player>) -> Unit) {
        playersCollection.get().addOnSuccessListener {
            result -> val players = result.map{
                it.toObject(Player::class.java)
            }
            callback(players)
        }
    }

    fun getPlayerByEmail(email : String, callback : (Player?) -> Unit) {
        playersCollection.whereEqualTo("email", email).get().addOnSuccessListener {
                result ->
            val player = result.documents.firstOrNull()?.toObject(Player::class.java)
            callback(player)
            _currentPlayer.value = player
        }
    }

    fun addWinToPlayer() {
        currentPlayer.value?.let {
            val updatedPlayer = it.copy(wins = it.wins + 1)
            playersCollection.document(it.id).set(updatedPlayer).addOnSuccessListener {
                _currentPlayer.update { updatedPlayer }
            }

        }
    }

    fun updateCurrentPlayerWins(callback: (Int) -> Unit) {
        currentPlayer.value?.let { player ->
            playersCollection.document(player.id).get().addOnSuccessListener { documentSnapshot ->
                val updatedPlayer = documentSnapshot.toObject(Player::class.java)
                updatedPlayer?.let {
                    callback(it.wins)

                }
            }
        }
    }

}
