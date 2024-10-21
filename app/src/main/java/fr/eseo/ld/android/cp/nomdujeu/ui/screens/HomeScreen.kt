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
import com.google.firebase.database.FirebaseDatabase
import fr.eseo.ld.android.cp.nomdujeu.service.WaitingRoom
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel, gameViewModel: GameViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isInWaitingRoom = remember { mutableStateOf(false) }
    val waitingRoom = remember { WaitingRoom(FirebaseDatabase.getInstance("https://nom-du-jeu-default-rtdb.europe-west1.firebasedatabase.app")) }
    val playerCount by waitingRoom.playerCount.collectAsState()

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
                        text = "Pseudo ici",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = "Wins ici",
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
                            text = "Joueurs en attente : $playerCount / 5",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Play/Cancel button at the bottom right
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                handlePlayButtonClick(
                                    context = context,
                                    navController = navController,
                                    isInWaitingRoom = isInWaitingRoom,
                                    waitingRoom = waitingRoom,
                                    gameViewModel = gameViewModel
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text(text = if (isInWaitingRoom.value) "Annuler" else "Lancer une partie")
                    }
                }
            }
        )
    }
}


suspend fun handlePlayButtonClick(
    context: Context,
    navController: NavController,
    isInWaitingRoom: MutableState<Boolean>,
    waitingRoom: WaitingRoom,
    gameViewModel: GameViewModel
) {

    val dispatcherIo: CoroutineDispatcher = Dispatchers.IO
    val dispatcherMain: CoroutineDispatcher = Dispatchers.Main

    if (isInWaitingRoom.value) {
        withContext(dispatcherIo) {
            waitingRoom.leaveRoom()
        }
        isInWaitingRoom.value = false
    } else {
        isInWaitingRoom.value = true
        withContext(dispatcherIo) {
            val isReady = waitingRoom.joinAndWait()
            withContext(dispatcherMain) {
                if (isReady) {
                    gameViewModel.launchGame(context, navController)
                } else {
                    Toast.makeText(
                        context,
                        "Erreur lors de la connection à la salle d'attente",
                        Toast.LENGTH_SHORT
                    ).show()
                    isInWaitingRoom.value = false
                }
            }
        }
    }
}
