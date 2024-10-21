package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.model.User
import fr.eseo.ld.android.cp.nomdujeu.repository.FirestoreRepository
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel

@Composable
fun HomeScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel, gameViewModel: GameViewModel) {

    val context = LocalContext.current


    BackHandler {
        // Ne rien faire ici permet de désactiver la fonction "retour en arrière" sur Android
    }

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ){
        Scaffold(
            content = {innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                ) {
                    Text(
                        text = "Bienvenue sur NomDuJeu",
                        style  = MaterialTheme.typography.bodyLarge,
                    )
                    Button(onClick = {
                            gameViewModel.launchGame(context, navController)
                    }) {
                        Text(text = "Lancer une partie")
                    }
                    Button(
                        onClick = {
                            authenticationViewModel.logout()
                            navController.navigate(NomDuJeuScreens.CONNECTION_SCREEN.id)
                        }
                    ) {
                        Text(text = "Deconnection")
                    }
                    Button(
                        onClick = {
                        }
                    ) {
                        Text(text = "Classement")
                    }
                }
            }
        )
    }
}
