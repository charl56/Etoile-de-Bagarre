package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.repository.AuthenticationRepository
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel() {

    lateinit var playerViewModel: PlayerViewModel

    private val _user = MutableLiveData<FirebaseUser?>()
    val user : MutableLiveData<FirebaseUser?>
        get() = _user

    // Update value when application is launched
    init{
        _user.value = authenticationRepository.getCurrentUser()

        if(_user.value == null){
            // TODO : besoin de faire qqch ici ? retour page login ? Sachant que on ne peut pas se connecter sans passer par la page de login
        }
    }



    fun signupWithEmail(email : String, password : String, pseudo: String) {
        authenticationRepository.signUpWithEmail(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    // Firebase User = use for auth with password and email
                    _user.value = authenticationRepository.getCurrentUser()
                    // Create Player, with email, pseudo, wins...
                    playerViewModel.addUser(email, pseudo)
                }
                else {
                    _user.value = null
                    playerViewModel.setUserNull()
                }
            }
    }

    fun loginWithEmail(email : String, password : String) {
        authenticationRepository.loginWithEmail(email, password).addOnCompleteListener{
                task ->
            if(task.isSuccessful) {
                // Firebase User = use for auth with password and email
                _user.value = authenticationRepository.getCurrentUser()
                // Get Player, with email, pseudo, wins...
                playerViewModel.getPlayerByEmail(email)
            }
            else {
                _user.value = null
                playerViewModel.setUserNull()
            }
        }
    }

    fun logout() {
        authenticationRepository.logout()
        playerViewModel.setUserNull()
    }

}


