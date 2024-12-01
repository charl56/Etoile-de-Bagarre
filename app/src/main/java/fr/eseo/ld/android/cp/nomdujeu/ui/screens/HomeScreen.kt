package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.GoogleAuthClient
import fr.eseo.ld.android.cp.nomdujeu.R
import fr.eseo.ld.android.cp.nomdujeu.model.Player
import fr.eseo.ld.android.cp.nomdujeu.service.WebSocket
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.ui.theme.NomDuJeuTheme
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.HandlePlay
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel,
    gameViewModel: GameViewModel,
    playerViewModel: PlayerViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isInWaitingRoom = remember { mutableStateOf(false) }
    val webSocket = WebSocket.getInstance()
    webSocket.InitViewModels(playerViewModel, gameViewModel)

    val playerCount by webSocket.playerCount.collectAsState()
    val currentUser by playerViewModel.player.collectAsState()
    val isWebSocketAvailable = remember { mutableStateOf(false) }
    val selectedPlayerCount = remember { mutableStateOf(2) }
    val googleAuthClient = GoogleAuthClient(context, playerViewModel)

    LaunchedEffect(Unit) {
        isWebSocketAvailable.value = webSocket.checkAvailability()
        playerViewModel.updateCurrentPlayerWins()
        Log.d("WinRate", "currnet mah ? ${currentUser?.wins}")
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
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    HomeScreenTop(
                        onDeconnection = {
                            coroutineScope.launch {
                                authenticationViewModel.logout()
                                googleAuthClient.signOut()
                                navController.navigate(NomDuJeuScreens.CONNECTION_SCREEN.id)
                            }
                        },
                        pseudo = currentUser?.pseudo
                            ?: stringResource(R.string.homeScreen_pseudoLoading),
                        numberWins = currentUser?.wins ?: 0
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        HomeScreenContent()
                    }
                    PlayerWaitingScreen(
                        selectedPlayerCount = selectedPlayerCount,
                        isWebSocketAvailable = isWebSocketAvailable,
                        isInWaitingRoom = isInWaitingRoom,
                        playerCount = playerCount,
                        currentUser = currentUser,
                        coroutineScope = coroutineScope,
                        context = context,
                        navController = navController,
                        gameViewModel = gameViewModel
                    )
                }
            }
        )
    }
}

@Composable
fun HomeScreenTop(
    onDeconnection: () -> Unit,
    pseudo: String,
    numberWins: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logout button at the top right
            Button(
                onClick = {
                    onDeconnection()
                },
                modifier = Modifier
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Text(text = stringResource(R.string.homeScreen_logout))
            }

            Text(
                text = pseudo,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            Text(
                text = "$numberWins ${stringResource(R.string.homeScreen_wins)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun HomeScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row() {
            CardSelectionScreen()
        }
    }
}


@Composable
fun loadImageFromAssets(fileName: String): ImageBitmap? {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(fileName) {
        withContext(Dispatchers.IO) {
            val inputStream = context.assets.open(fileName)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageBitmap = bitmap.asImageBitmap()
        }
    }

    return imageBitmap
}

@Composable
fun PlayerCard(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Define the frames for idle and attack animations
    val idleFrames = listOf(
        "player/idle_00.png",
        "player/idle_01.png",
        "player/idle_02.png",
        "player/idle_03.png",
        "player/idle_04.png",
        "player/idle_05.png"
    )
    val attackFrames = listOf(
        "player/attack_00.png",
        "player/attack_01.png",
        "player/attack_02.png",
        "player/attack_03.png"
    )

    // Track the current frame and animation state
    var currentFrame by remember { mutableStateOf(0) }
    var isIdle by remember { mutableStateOf(true) }

    // Frame duration
    val frameDuration = 100L

    // Handle animation state and frame updates
    LaunchedEffect(isIdle) {
        while (true) {
            delay(frameDuration)
            currentFrame = (currentFrame + 1) % (if (isIdle) idleFrames.size else attackFrames.size)
            if (!isIdle && currentFrame == attackFrames.size - 1) {
                isIdle = true
                currentFrame = 0
            }
        }
    }

    // Load the current frame image from assets
    val currentImage = loadImageFromAssets(if (isIdle) idleFrames[currentFrame] else attackFrames[currentFrame])

    // Display the card with the current animation frame
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(25.dp))
            .border(
                BorderStroke(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                isIdle = false
                currentFrame = 0
                onClick()
            }
    ) {
        currentImage?.let {
            Image(
                bitmap = it,
                contentDescription = "Animated character",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(25.dp))
            )
        }
    }
}

@Composable
fun PlayerNullCard(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(25.dp))
            .border(
                BorderStroke(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "New characters will be available soon.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CardSelectionScreen() {
    var selectedCard by remember { mutableStateOf("PlayerCard") }

    Row {
        PlayerCard(
            isSelected = selectedCard == "PlayerCard",
            onClick = { selectedCard = "PlayerCard" }
        )
        Spacer(modifier = Modifier.width(16.dp))
        PlayerNullCard(
            isSelected = selectedCard == "PlayerNullCard",
            onClick = {}
        )
    }
}

@Composable
fun PlayerWaitingScreen(
    selectedPlayerCount: MutableState<Int>,
    isWebSocketAvailable: MutableState<Boolean>,
    isInWaitingRoom: MutableState<Boolean>,
    playerCount: Int,
    currentUser: Player?,
    coroutineScope: CoroutineScope,
    context: Context,
    navController: NavController,
    gameViewModel: GameViewModel
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
        Row(
            modifier = Modifier
                .padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.homeScreen_playersInGame),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            listOf(2, 3, 4).forEach { count ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = selectedPlayerCount.value == count,
                        onClick = { selectedPlayerCount.value = count },
                        enabled = (isWebSocketAvailable.value && !isInWaitingRoom.value) // Enable choice only if server is available and not in waiting room
                    )
                    Text(text = "$count ${stringResource(R.string.homeScreen_playersCount)}")
                }
            }

            // Loader when waiting, centered on the screen
            if (isInWaitingRoom.value) {
                Text(
                    text = "${stringResource(R.string.homeScreen_waitingPlayer)} : $playerCount / ${selectedPlayerCount.value}",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
            }
            // Play/Cancel button at the bottom right
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                Button(
                    onClick = {
                        if (currentUser != null) {
                            //gameViewModel.launchGame(context, navController)
                            coroutineScope.launch {
                                HandlePlay().handlePlayButtonClick(
                                    context = context,
                                    navController = navController,
                                    isInWaitingRoom = isInWaitingRoom,
                                    gameViewModel = gameViewModel,
                                    currentPlayer = currentUser,
                                    selectedPlayerCount = selectedPlayerCount.value,
                                    isWebSocketAvailable = isWebSocketAvailable
                                )
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "${R.string.homeScreen_error_connectMatchMaking}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    enabled = isWebSocketAvailable.value
                ) {
                    Text(
                        text = if (isInWaitingRoom.value) stringResource(R.string.homeScreen_stopMatchMaking) else stringResource(
                            R.string.homeScreen_startMatchMaking
                        )
                    )
                }
            }
            if (!isWebSocketAvailable.value) {
                Text(
                    text = stringResource(R.string.homeScreen_error_serverNotAvailable),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 640,
    heightDp = 360
)
@Composable
fun HomeScreenPreview() {
    NomDuJeuTheme(darkTheme = false, dynamicColor = false) {
        HomeScreenTop(
            onDeconnection = {},
            pseudo = "Pseudo",
            numberWins = 0
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 640,
    heightDp = 360
)
@Composable
fun HomeScreenPreviewContent() {
    NomDuJeuTheme(darkTheme = false, dynamicColor = false) {
        HomeScreenContent()
    }
}
