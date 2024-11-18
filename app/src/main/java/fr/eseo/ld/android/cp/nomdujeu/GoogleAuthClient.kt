package fr.eseo.ld.android.cp.nomdujeu

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val context: Context,
    private val playerViewModel: PlayerViewModel
) {
    private val tag = "GoogleAuthClient: "

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isSignedIn(): Boolean {
        if(firebaseAuth.currentUser != null){
            println(tag + "already signed in")
            return true
        }
        return false
    }

    suspend fun signIn(): Boolean{

        if (isSignedIn()) {
            val user = firebaseAuth.currentUser
            return true
        }

        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "signIn error : ${e.message}")
            return false

        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
                    .setServerClientId("266100145988-ef4r1enhivm85q5s9ujeivra4sbk4ldq.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return credentialManager.getCredential(request = request, context = context)
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {

                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                println(tag + "name: ${tokenCredential.displayName}")
                println(tag + "email: ${tokenCredential.id}")
                println(tag + "photo: ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    createPlayerIfNew(user.email, user.displayName)
                    println(tag + "name user: ${user.displayName}")
                    println(tag + "email user: ${user.email}")
                    println(tag + "photo user: ${user.photoUrl}")
                    println(tag + "user signed: $user")
                    return true
                }
                return false

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "GoogleIdTokenParsingException : ${e.message}")
                return false

            }
        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false

        }
    }

    private fun createPlayerIfNew(email: String?, displayName: String?) {
        playerViewModel.getPlayerByEmail(email!!)
        println(tag + "player")
        println(tag + "email: $email")
        println(tag + "displayName: $displayName")
        playerViewModel.addUser(email, displayName!!)
    }

}