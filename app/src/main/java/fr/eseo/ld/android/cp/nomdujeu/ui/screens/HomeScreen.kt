package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun HomeScreen (
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel,
    gameViewModel: GameViewModel,
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isInWaitingRoom = remember { mutableStateOf(false) }
    val webSocket = remember { WebSocket() }
    val players by webSocket.players.collectAsState()       // List of players in the waiting room
    val currentUser by playerViewModel.player.collectAsState()  // Current user
    val isWebSocketAvailable = remember { mutableStateOf(false) }   // Is the websocket available

    // Check if websocket is available
    LaunchedEffect(Unit) {
        isWebSocketAvailable.value = webSocket.checkAvailability()
    }

    BackHandler {
        // Doing nothing here, so the back button in android is disabled
    }

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Scaffold(
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    // Logout button at the top right
                    Button(
                        onClick = {
                            authenticationViewModel.logout()
                            navController.navigate(NomDuJeuScreens.CONNECTION_SCREEN.id)
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, end = 16.dp)
                    ) {
                        Text(text = "Deconnection")
                    }

                    Text(
                        text = currentUser?.pseudo ?: "Pseudo loading...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = "${currentUser?.wins ?: 0 } Win(s)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp)
                    )

                    Text(
                        text = "Choix des persos ici",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 16.dp)
                    )

                    // Loader when waiting, centered on the screen
                    if (isInWaitingRoom.value) {
                        Text(
                            text = "Joueurs en attente : ${players.size} / 5",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Play/Cancel button at the bottom right
                    Button(
                        onClick = {
                            if(currentUser != null) {
                                coroutineScope.launch {
                                    HandlePlay().handlePlayButtonClick(
                                        context = context,
                                        navController = navController,
                                        isInWaitingRoom = isInWaitingRoom,
                                        webSocket = webSocket,
                                        gameViewModel = gameViewModel,
                                        currentPlayer = currentUser!!
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Error when trying to connect to match making. Try to restart game or relog in", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        enabled = isWebSocketAvailable.value && !isInWaitingRoom.value
                    ) {
                        Text(text = if (isInWaitingRoom.value) "Annuler" else "Lancer une partie")
                    }
                    if (!isWebSocketAvailable.value) {
                        Text(
                            text = "Server isn't available now",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 64.dp)
                        )
                    }
                }
            }
        )
    }
}


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
        currentPlayer: Player
    ) {
        if (isInWaitingRoom.value) {        // Stop websocket when leaving the waiting room
            withContext(dispatcherIo) {
                webSocket.leaveRoom()
            }
            isInWaitingRoom.value = false
        } else {                            // Join the waiting room : start websocket
            isInWaitingRoom.value = true
            withContext(dispatcherIo) {
                launch {
                    webSocket.gameStarted.collect { started ->
                        if (started) {
                            withContext(dispatcherMain) {
                                gameViewModel.launchGame(context, navController)
                            }
                        }
                    }
                }
                webSocket.joinAndWait(currentPlayer)
            }
            withContext(dispatcherMain) {
                if (!webSocket.gameStarted.value) {
                    Toast.makeText(
                        context,
                        "Erreur lors de la connexion à la salle d'attente",
                        Toast.LENGTH_SHORT
                    ).show()
                    isInWaitingRoom.value = false
                }
            }
        }
    }
}