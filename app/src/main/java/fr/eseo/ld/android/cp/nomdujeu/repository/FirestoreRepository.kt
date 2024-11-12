package fr.eseo.ld.android.cp.nomdujeu.repository

import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    fun addWinToPlayerWithId(playerId : String) {
        playersCollection.document(playerId).get().addOnSuccessListener {
            val player = it.toObject(Player::class.java)
            player?.let {
                val updatedPlayer = it.copy(wins = it.wins + 1)
                playersCollection.document(playerId).set(updatedPlayer)
            }
        }
    }


}
