package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel

@Composable
fun EndGameScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel, gameViewModel: GameViewModel) {

    val context = LocalContext.current

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
                    Text(text = "Fin de la partie")
                    Button(onClick = {
                        gameViewModel.launchGame(context, navController)

                    }) {
                        Text(text = "Rejouer")
                    }
                    Button(onClick = {
                        navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
                    }) {
                        Text(text = "Quitter")
                    }
                }
            }
        )
    }
}
