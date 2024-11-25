package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
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
    Box(modifier = Modifier
        .padding(innerPadding)
        .fillMaxWidth()
        .background(
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

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun PlayerRankingScreen(webSocket: WebSocket) {
    // current player
    val player by webSocket.player.collectAsState(null)
    // other players
    val players by webSocket.players.collectAsState(emptyList())
    val combinedPlayers = mutableListOf<Player>().apply {
        addAll(players)
        player?.let { add(it) }
    }
    val sortedPlayers = combinedPlayers.sortedWith(compareByDescending<Player> { it.life }.thenByDescending { it.kills })

    Log.d("EndGameScreen", "Players: $combinedPlayers")
    Log.d("EndGameScreen", "Player count: ${combinedPlayers.size} - ${webSocket.playerCount.value}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.ranking),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        TableHeader()
        LazyColumn {
            itemsIndexed(sortedPlayers) { index, player ->
                PlayerRankingItem(index + 1, player)
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
        Text(text = stringResource(R.string.rank), style = MaterialTheme.typography.bodyLarge)
        Text(text = stringResource(R.string.pseudo), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PlayerRankingItem(rank: Int, player: Player) {
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
            Text(
                text = "$rank${getOrdinalSuffix(rank)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = player.pseudo,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun getOrdinalSuffix(rank: Int): String {
    return when {
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
}


