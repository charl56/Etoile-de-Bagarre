package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.R
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens

@Composable
fun EndGameScreen(
    navController: NavController,
) {

    // Need to be refactor ?
    var webSocket = WebSocket.getInstance()
    var winner = webSocket.winner;
    var kills = webSocket.kills;
    var endString = "$winner à gagné avec $kills kills"

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ){
        Scaffold(
            content = {innerPadding ->
                Box(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                ) {
                    Text(
                        text = endString,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    Button(
                        onClick = {
                        navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
                    },
                        modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(top = 16.dp, end = 16.dp)

                    ) {
                        Text(text = "${stringResource(R.string.endGameScreen_exit)}")
                    }
                }
            }
        )
    }
}


