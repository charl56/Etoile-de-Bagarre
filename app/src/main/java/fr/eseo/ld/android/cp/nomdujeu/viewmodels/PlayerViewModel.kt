package fr.eseo.ld.android.cp.nomdujeu.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.repository.AuthenticationRepository
import fr.eseo.ld.android.cp.nomdujeu.repository.FirestoreRepository
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val repository: FirestoreRepository,
    private val authenticationRepository: AuthenticationRepository

) : AndroidViewModel(application) {

    private val _player = MutableStateFlow<Player?>(null)
    val player : StateFlow<Player?> = _player.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players : StateFlow<List<Player>> = _players.asStateFlow()

    init {
        // Update value when application is launched (here actual player)
        if(authenticationRepository.getCurrentUser() != null){
            getPlayerByEmail(authenticationRepository.getCurrentUser()?.email!!)
        }
    }



    fun addUser(email: String, pseudo: String) {
        val player = Player(email = email, pseudo = pseudo)
        repository.addPlayer(player)
        getPlayerByEmail(email)
    }


    fun getPlayerByEmail(email: String) {
        repository.getPlayerByEmail(email) { user ->
            _player.value = user
            // Update player in WebSocket
            WebSocket.getInstance().setPlayer(user!!)
        }
    }

    // TODO : call when a player win a game
    fun addWinToUserWithId(userId : String) {
        repository.addWinToPlayerWithId(userId)
    }

    fun setUserNull(){
        _player.value = null
    }

}


