package fr.eseo.ld.android.cp.nomdujeu.repository

import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")


    fun addUser(player : Player) {
        if (player.id.isEmpty()) {
            player.id = usersCollection.document().id
        }
        usersCollection.document(player.id).set(player)
    }

    fun getUsers(callback : (List<Player>) -> Unit) {
        usersCollection.get().addOnSuccessListener {
            result -> val players = result.map{
                it.toObject(Player::class.java)
            }
            callback(players)
        }
    }

    fun getUserByEmail(email : String, callback : (Player?) -> Unit) {
        usersCollection.whereEqualTo("email", email).get().addOnSuccessListener {
                result ->
            val player = result.documents.firstOrNull()?.toObject(Player::class.java)
            callback(player)
        }
    }


    fun addWinToUserWithId(userId : String) {
    }


}
