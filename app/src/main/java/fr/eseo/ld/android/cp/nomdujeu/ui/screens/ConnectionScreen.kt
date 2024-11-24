package fr.eseo.ld.android.cp.nomdujeu.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.GoogleAuthClient
import fr.eseo.ld.android.cp.nomdujeu.R
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch


@Composable
fun ConnectionScreen(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel,
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val googleAuthClient = GoogleAuthClient(context, playerViewModel)
    val errorMessage by authenticationViewModel.errorMessage.observeAsState()
    val isLoginSuccessful by authenticationViewModel.isLoginSuccessful.observeAsState()
    val isSignUpSuccessful by authenticationViewModel.isSignUpSuccessful.observeAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isSignIn by remember { mutableStateOf(googleAuthClient.isSignedIn()) }
    var lifecycleScope = rememberCoroutineScope()

    // Show toast when error message changes
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Log.d("Authentication", "Displaying error message: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            authenticationViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful == true) {
            navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
            authenticationViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(isSignUpSuccessful) {
        if (isSignUpSuccessful == true) {
            navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
            authenticationViewModel.clearErrorMessage()
        }
    }

    ConnectionScreenContent(
        email = email,
        onEmailChange = { email = it; authenticationViewModel.clearErrorMessage() },
        password = password,
        onPasswordChange = { password = it; authenticationViewModel.clearErrorMessage() },
        username = username,
        onUsernameChange = { username = it; authenticationViewModel.clearErrorMessage() },
        onLoginClick = {
            authenticationViewModel.loginWithEmail(email, password)
            Log.d("Authentication", "Login clicked : " + authenticationViewModel.errorMessage.value)
        },
        onSignupClick = {
            authenticationViewModel.signupWithEmail(email, password, username)
            navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
        },
        onGoogleSignIn = {
            if(!isSignIn){
                lifecycleScope.launch {
                    val success = googleAuthClient.signIn()
                    if (success){
                        navController.navigate(NomDuJeuScreens.HOME_SCREEN.id)
                    }
                }
            }
        }
    )
}

@Composable
fun ConnectionScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var isLoginScreen by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isLoginScreen) {
                            LoginContent(
                                email = email,
                                onEmailChange = onEmailChange,
                                password = password,
                                onPasswordChange = onPasswordChange,
                                onLoginClick = onLoginClick,
                                onNavigateToSignUp = { isLoginScreen = false },
                                onGoogleSignIn = onGoogleSignIn
                            )
                        } else {
                            SignUpContent(
                                email = email,
                                onEmailChange = onEmailChange,
                                password = password,
                                onPasswordChange = onPasswordChange,
                                onSignupClick = onSignupClick,
                                onNavigateToLogin = { isLoginScreen = true },
                                username = username,
                                onUsernameChange = onUsernameChange
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.loginTitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = stringResource(id = R.string.loginMessage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = onNavigateToSignUp) {
                Text(
                    text = stringResource(id = R.string.signup),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(id = R.string.email)) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            IconButton(onClick = onGoogleSignIn) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_google),
                    contentDescription = stringResource(id = R.string.signinGoogle),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onLoginClick) {
                Text(
                    text = stringResource(id = R.string.login),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SignUpContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onSignupClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.signUpTitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = stringResource(id = R.string.signUpMessage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(id = R.string.login),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(id = R.string.username)) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(id = R.string.email)) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onSignupClick) {
                Text(
                    text = stringResource(id = R.string.signup),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


