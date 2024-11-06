package fr.eseo.ld.android.cp.nomdujeu.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
class GameViewModel : ViewModel() {

    private var gameLaunched = false
    private val handler = Handler(Looper.getMainLooper()) // TODO : à supprimer, permet de lancer endGame après 5 sec

    fun launchGame(context: Context, navController: NavController) {
        if (!gameLaunched) {
            val intent = Intent(context, AndroidLauncher::class.java)
            context.startActivity(intent)
            gameLaunched = true

            // TODO : à supprimer, permet de lancer endGame après 5 sec
            handler.postDelayed({
                endGame(navController)
            }, 5000)
        }
    }


    fun endGame(navController: NavController) {
        gameLaunched = false
        AndroidLauncher.exitGame()
        // Leave game room, but stay connected to websocket
        viewModelScope.launch {
            val webSocket = WebSocket.getInstance()
            webSocket.leaveRoom()
            navController.navigate(NomDuJeuScreens.END_GAME_SCREEN.id)
        }

    }

}

// Class who manage the connection to the waiting room, leaving, and the launch of the game
class HandlePlay(
    private val dispatcherIo: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main
) {
    suspend fun handlePlayButtonClick(
        context: Context,
        navController: NavController,
        isInWaitingRoom: MutableState<Boolean>,
        webSocket: WebSocket,
        gameViewModel: GameViewModel,
        currentPlayer: Player,
        isWebSocketAvailable: MutableState<Boolean>
    ) {
        if (isInWaitingRoom.value) {        // Leave the waiting room, but stay connected to websocket
            withContext(dispatcherIo) {
                webSocket.leaveRoom()
            }
            isInWaitingRoom.value = false
        } else {                            // Join the waiting room in websocket
            isInWaitingRoom.value = true
            withContext(dispatcherIo) {
                // Waiting that websocket say "game started", when the room is full
                launch {
                    webSocket.gameStarted.collect { started ->
                        if (started) {
                            withContext(dispatcherMain) {
                                gameViewModel.launchGame(context, navController)
                            }
                        }
                    }
                }
                // The waiting start with this function
                try {
                    webSocket.joinAndWait(currentPlayer)
                } catch (e: Exception) {
                    println("WEBSOCKET: Error while joining the waiting room: ${e.message}")
                    isWebSocketAvailable.value = webSocket.checkAvailability()

                }
            }
        }
    }
}


