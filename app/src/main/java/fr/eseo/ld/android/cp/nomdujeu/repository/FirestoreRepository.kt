package fr.eseo.ld.android.cp.nomdujeu.repository

import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.android.cp.nomdujeu.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    fun addUser(user : User) {
        if (user.id.isEmpty()) {
            user.id = usersCollection.document().id
        }
        usersCollection.document(user.id).set(user)
    }

    fun getUserByEmail(email : String, callback : (User?) -> Unit) {
        usersCollection.whereEqualTo("email", email).get().addOnSuccessListener {
                result ->
            val user = result.documents.firstOrNull()?.toObject(User::class.java)
            callback(user)
        }
    }


    fun addWinToUserWithId(userId : String) {
    }


}
