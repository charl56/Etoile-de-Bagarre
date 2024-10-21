package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.model.Player
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

    private val _player = MutableStateFlow<Player?>(null)
    val player : StateFlow<Player?> = _player.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players : StateFlow<List<Player>> = _players.asStateFlow()


    fun addUser(email: String, pseudo: String) {
        val player = Player(email = email, pseudo = pseudo)
        repository.addUser(player)
        getUserByEmail(email)
    }


    fun getUserByEmail(email: String) {
        repository.getUserByEmail(email) { user ->
            _player.value = user
        }
    }

    // TODO : call when a player win a game
    fun addWinToUserWithId(userId : String) {
        repository.addWinToUserWithId(userId)
    }

    fun setUserNull(){
        _player.value = null
    }

}


