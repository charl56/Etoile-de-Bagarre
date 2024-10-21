package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import fr.eseo.ld.android.cp.nomdujeu.service.WaitingRoom
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel, gameViewModel: GameViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isInWaitingRoom = remember { mutableStateOf(false) }
    val waitingRoom = remember { WaitingRoom(FirebaseDatabase.getInstance()) }

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
                Column(
                    modifier = Modifier.padding(innerPadding),
                ) {
                    Text(
                        text = "Bienvenue sur NomDuJeu",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (isInWaitingRoom.value) {

                                    waitingRoom.leaveRoom()
                                    isInWaitingRoom.value = false
                                } else {
                                    isInWaitingRoom.value = true
                                    val isReady = waitingRoom.joinAndWait()
                                    if (isReady) {
                                        gameViewModel.launchGame(context, navController)
                                    } else {
                                        // Show error message
                                        Toast.makeText(context, "Erreur lors de la connection à la salle d'attente", Toast.LENGTH_SHORT).show()
                                        isInWaitingRoom.value = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text(text = if (isInWaitingRoom.value) "Annuler" else "Lancer une partie")
                    }
                    Button(
                        onClick = {
                            authenticationViewModel.logout()
                            navController.navigate(NomDuJeuScreens.CONNECTION_SCREEN.id)
                        }
                    ) {
                        Text(text = "Deconnection")
                    }
                }
            }
        )
    }
}