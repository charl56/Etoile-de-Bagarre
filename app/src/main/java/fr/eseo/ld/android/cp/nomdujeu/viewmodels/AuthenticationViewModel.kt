package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.repository.AuthenticationRepository
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) : ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>()
    val user : MutableLiveData<FirebaseUser?>
        get() = _user

    init{
        _user.value = authenticationRepository.getCurrentUser()
        if(_user.value == null){
            // TODO : rediriger vers la page de connection ? Ou cela se fait tout seul ?
        }
    }


    fun signupWithEmail(email : String, password : String) {
        authenticationRepository.signUpWithEmail(email, password)
            .addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    _user.value = authenticationRepository.getCurrentUser()
                }
                else {
                    _user.value = null
                }
            }
    }

    fun loginWithEmail(email : String, password : String) {
        authenticationRepository.loginWithEmail(email, password).addOnCompleteListener{
                task ->
            if(task.isSuccessful) {
                _user.value = authenticationRepository.getCurrentUser()
            }
            else {
                _user.value = null
            }
        }
    }

    fun logout() {
        authenticationRepository.logout()
    }

}


