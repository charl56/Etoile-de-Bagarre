package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.repository.AuthenticationRepository
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) : ViewModel() {

    lateinit var userViewModel: UserViewModel

    // User firebase (email, password, Id) != User app
    private val _user = MutableLiveData<FirebaseUser?>()
    val user : MutableLiveData<FirebaseUser?>
        get() = _user

    init{
        // Update value when application is launched
        _user.value = authenticationRepository.getCurrentUser()

        if(_user.value == null){
            // TODO : besoin de faire qqch ici ?
        }
    }


    fun signupWithEmail(email : String, password : String, pseudo: String) {
        authenticationRepository.signUpWithEmail(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    // Firebase User != game user
                    _user.value = authenticationRepository.getCurrentUser()
                    // Create User game, with email, pseudo, wins...
                    userViewModel.addUser(email, pseudo)
                }
                else {
                    _user.value = null
                    userViewModel.setUserNull()
                }
            }
    }

    fun loginWithEmail(email : String, password : String) {
        authenticationRepository.loginWithEmail(email, password).addOnCompleteListener{
                task ->
            if(task.isSuccessful) {
                // Firebase User != game user
                _user.value = authenticationRepository.getCurrentUser()
                // Get User game, with email, pseudo, wins...
                userViewModel.getUserByEmail(email)
            }
            else {
                _user.value = null
                userViewModel.setUserNull()
            }
        }
    }

    fun logout() {
        authenticationRepository.logout()
    }

}


