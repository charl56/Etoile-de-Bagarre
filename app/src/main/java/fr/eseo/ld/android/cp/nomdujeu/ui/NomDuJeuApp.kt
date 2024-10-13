package fr.eseo.ld.android.cp.nomdujeu.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.NomDuJeuViewModel
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.ui.screens.ConnectionScreen
import fr.eseo.ld.android.cp.nomdujeu.ui.screens.HomeScreen
import fr.eseo.ld.android.cp.nomdujeu.ui.screens.WaitingScreen
import fr.eseo.ld.android.cp.nomdujeu.ui.screens.GameScreen
import fr.eseo.ld.android.cp.nomdujeu.ui.screens.EndGameScreen



@Composable
fun NomDuJeuApp() {
    val navController: NavHostController = rememberNavController()
    val viewModel : NomDuJeuViewModel = hiltViewModel()
    val authenticationViewModel : AuthenticationViewModel = hiltViewModel()

    NavHost(navController , startDestination = "start") {

        composable("start") {
            val user by authenticationViewModel.user.observeAsState()
            LaunchedEffect(user) {
                if(user == null) {
                    // TODO : modifier l'ecran en : NomDuJeuScreens.CONNECTION_SCREEN.id. Changement pour facilité de dev
                    navController.navigate(NomDuJeuScreens.CONNECTION_SCREEN.id) {
                        popUpTo("start"){inclusive = true}
                    }
                } else {
                    navController.navigate(NomDuJeuScreens.HOME_SCREEN.id) {
                        popUpTo("start"){inclusive = true}
                    }
                }
            }
        }

        composable(NomDuJeuScreens.CONNECTION_SCREEN.id) {
            ConnectionScreen(navController, authenticationViewModel)
        }

        composable(NomDuJeuScreens.HOME_SCREEN.id) {
            HomeScreen(navController, authenticationViewModel)
        }

        composable(NomDuJeuScreens.WAITING_SCREEN.id) {
            WaitingScreen(navController, authenticationViewModel)
        }

        composable(NomDuJeuScreens.GAME_SCREEN.id) {
            GameScreen(navController, authenticationViewModel)
        }

        composable(NomDuJeuScreens.END_GAME_SCREEN.id) {
            EndGameScreen(navController, authenticationViewModel)
        }
    }
}
