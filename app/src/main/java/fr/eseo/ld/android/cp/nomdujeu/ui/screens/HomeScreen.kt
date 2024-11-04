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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.R
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
    val webSocket = WebSocket.getInstance()

    val playerCount by webSocket.playerCount.collectAsState()       // List of players in the waiting room
    val currentUser by playerViewModel.player.collectAsState()  // Current user
    val isWebSocketAvailable = remember { mutableStateOf(false) }   // Is the websocket available

    // Check if websocket is available
    LaunchedEffect(Unit) {
        println("WEBSOCKET: Check availability")
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
                        Text(text = stringResource(R.string.homeScreen_logout))
                    }

                    Text(
                        text = currentUser?.pseudo ?: stringResource(R.string.homeScreen_pseudoLoading),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = "${currentUser?.wins ?: 0 } ${stringResource(R.string.homeScreen_wins)}",
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
                            text = "${stringResource(R.string.homeScreen_waitingPlayer)} : $playerCount / 5",
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
                                        currentPlayer = currentUser!!,
                                        isWebSocketAvailable = isWebSocketAvailable
                                    )
                                }
                            } else {
                                Toast.makeText(context, "${R.string.homeScreen_error_connectMatchMaking}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        enabled = isWebSocketAvailable.value
                    ) {
                        Text(text = if (isInWaitingRoom.value) "${stringResource(R.string.homeScreen_stopMatchMaking)}" else "${stringResource(R.string.homeScreen_startMatchMaking)}")
                    }
                    if (!isWebSocketAvailable.value) {
                        Text(
                            text = "${stringResource(R.string.homeScreen_error_serverNotAvailable)}",
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