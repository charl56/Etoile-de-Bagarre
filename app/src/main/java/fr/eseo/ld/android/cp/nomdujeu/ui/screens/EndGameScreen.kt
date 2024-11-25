package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.R
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import kotlinx.coroutines.launch

@Composable
fun EndGameScreen(
    navController: NavController,
) {

    // Need to be refactor ?
    val webSocket = WebSocket.getInstance()
    val winner = webSocket.winner
    val kills = webSocket.kills
    val endString = "$winner win with $kills kills"
    Log.d("EndGameScreen", "Winner: $winner, Kills: $kills")

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
                    Column {
                        EndGameTopBar(innerPadding, endString)
                        PlayerRankingScreen(webSocket)
                    }
                    Button(
                        onClick = {
                        navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
                    },
                        modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)

                    ) {
                        Text(text = "${stringResource(R.string.endGameScreen_exit)}")
                    }
                }
            }
        )
    }
}

@Composable
fun EndGameTopBar(innerPadding: PaddingValues, endString: String){
    Box(modifier = Modifier.padding(innerPadding).fillMaxWidth().background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.primary
            )
        )
    )){
        Text(
            text = endString,
            modifier = Modifier.align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun PlayerRankingScreen(webSocket: WebSocket) {
    val players = fetchPlayers(webSocket)
    val sortedPlayers = players.sortedByDescending { it.kills }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Classement", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally))
        TableHeader()
        LazyColumn {
            items(sortedPlayers) { player ->
                PlayerRankingItem(player)
            }
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Pseudo", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Kills", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PlayerRankingItem(player: Player) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = player.pseudo, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${player.kills} kills", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun fetchPlayers(webSocket: WebSocket): List<Player> {
    var players by remember { mutableStateOf(emptyList<Player>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            webSocket.players.collect { playerList ->
                players = playerList
                Log.d("EndGameScreen", "Players: $players")
            }
        }
    }

    return players
}


