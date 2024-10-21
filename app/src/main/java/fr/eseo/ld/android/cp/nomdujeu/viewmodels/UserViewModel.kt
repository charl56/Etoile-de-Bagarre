package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.model.User
import fr.eseo.ld.android.cp.nomdujeu.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    private val repository: FirestoreRepository

) : AndroidViewModel(application) {

    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users : StateFlow<List<User>> = _users.asStateFlow()


    // TODO : call this method after sign up with authentication,and result OK
    fun add(note: User) {
        repository.addUser(note)
    }

    fun getUsers() {
        repository.getUsers { notes ->
            _users.value = notes
        }
    }

    // TODO : call this method after login with authentication and return OK, before change screen. Save data in a companion object (global var)
    fun getUserByEmail(email: String) {
        repository.getUserByEmail(email) { user ->
            _user.value = user
        }
    }

    // TODO : call when a player win a game
    fun addWinToUserWithId(userId : String) {
        repository.addWinToUserWithId(userId)
    }

}


