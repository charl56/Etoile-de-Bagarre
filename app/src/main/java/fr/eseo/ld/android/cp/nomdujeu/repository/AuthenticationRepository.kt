package fr.eseo.ld.android.cp.nomdujeu.repository

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthenticationRepository @Inject constructor (
    private val firebaseAuth : FirebaseAuth
) {

    fun signUpWithEmail(email : String, password : String) = firebaseAuth.createUserWithEmailAndPassword(email, password)
    fun loginWithEmail(email : String, password : String) = firebaseAuth.signInWithEmailAndPassword(email, password)
    fun logout() = firebaseAuth.signOut()

    fun getCurrentUser() = firebaseAuth.currentUser

}