package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction
import fr.eseo.ld.android.cp.nomdujeu.AndroidLauncher
import fr.eseo.ld.android.cp.nomdujeu.Main
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel

@Composable
fun GameScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel) {

    val context = LocalContext.current

    // Lancer le jeu lorsque l'écran est chargé
    LaunchedEffect(Unit) {
        println(System.getProperty("os.name"))

        val intent = Intent(context, AndroidLauncher::class.java)
        context.startActivity(intent)
    }


    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
            .background(Color.Red)
    ){
        Scaffold (
            content = {innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                ) {
                    Text(text = "Game Screen")
                }

            }
        )
    }
}

// Fonction pour lancer l'activité AndroidLauncher
fun launchGame(context: Context) {
    val intent = Intent(context, AndroidLauncher::class.java)
    context.startActivity(intent)
}