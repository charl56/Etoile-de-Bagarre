package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import android.util.Patterns
import androidx.lifecycle.LiveData
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

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage
    private val _isLoginSuccessful = MutableLiveData<Boolean?>()
    val isLoginSuccessful: LiveData<Boolean?>
        get() = _isLoginSuccessful
    private val _isSignUpSuccessful = MutableLiveData<Boolean?>()
    val isSignUpSuccessful: LiveData<Boolean?>
        get() = _isSignUpSuccessful
    private val _user = MutableLiveData<FirebaseUser?>()
    val user : MutableLiveData<FirebaseUser?>
        get() = _user

    // Update value when application is launched
    init{
        _user.value = authenticationRepository.getCurrentUser()
    }



    fun signupWithEmail(email : String, password : String, pseudo: String) {
        if (email.isEmpty() || password.isEmpty() || pseudo.isEmpty()) {
            _errorMessage.value = "Please fill in all fields."
            _isSignUpSuccessful.value = false
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            _isSignUpSuccessful.value = false
            return
        }
        authenticationRepository.signUpWithEmail(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    // Firebase User = use for auth with password and email
                    _user.value = authenticationRepository.getCurrentUser()
                    // Create Player, with email, pseudo, wins...
                    playerViewModel.addUser(email, pseudo)
                    _isSignUpSuccessful.value = true
                }
                else {
                    _user.value = null
                    playerViewModel.setUserNull()
                    _errorMessage.value = "Signup failed. Please try again."
                    _isSignUpSuccessful.value = false
                }
            }
    }

    fun loginWithEmail(email : String, password : String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Please fill in all fields."
            _isLoginSuccessful.value = false
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            _isLoginSuccessful.value = false
            return
        }
        authenticationRepository.loginWithEmail(email, password).addOnCompleteListener{
                task ->
            if(task.isSuccessful) {
                // Firebase User = use for auth with password and email
                _user.value = authenticationRepository.getCurrentUser()
                // Get Player, with email, pseudo, wins...
                playerViewModel.getPlayerByEmail(email)
                _isLoginSuccessful.value = true
            }
            else {
                _user.value = null
                playerViewModel.setUserNull()
                if (!task.isSuccessful) {
                    _errorMessage.value = "Login failed. Please try again."
                    _isLoginSuccessful.value = false
                }
            }
        }
    }

    fun logout() {
        authenticationRepository.logout()
        playerViewModel.setUserNull()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        _isLoginSuccessful.value = null
        _isSignUpSuccessful.value = null
    }

}


