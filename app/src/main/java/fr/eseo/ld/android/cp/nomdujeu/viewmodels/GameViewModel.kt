package fr.eseo.ld.android.cp.nomdujeu.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.game.AndroidLauncher
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class GameViewModel : ViewModel() {

    private var gameLaunched = false
    private val handler = Handler(Looper.getMainLooper()) // TODO : à supprimer, permet de lancer endGame après 5 sec

    fun launchGame(context: Context, navController: NavController) {
        if (!gameLaunched) {
            val intent = Intent(context, AndroidLauncher::class.java)
            context.startActivity(intent)
            gameLaunched = true

            // TODO : mettre en palce un singleton qui se connecte à la BDD, pouir envoyer et recupérer les données (position, vie...)

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


