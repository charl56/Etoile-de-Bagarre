package fr.eseo.ld.android.cp.nomdujeu.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.eseo.ld.android.cp.nomdujeu.R
import fr.eseo.ld.android.cp.nomdujeu.ui.theme.NomDuJeuTheme
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel

@Composable
fun ConnectionScreen(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ConnectionScreenContent(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLoginClick = {
            authenticationViewModel.loginWithEmail(email, password)
            navController.popBackStack()
        },
        onSignupClick = {
            authenticationViewModel.signupWithEmail(email, password)
            navController.popBackStack()
        },

    )
}

@Composable
fun ConnectionScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
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
                                onNavigateToSignUp = { isLoginScreen = false }
                            )
                        } else {
                            SignUpContent(
                                email = email,
                                onEmailChange = onEmailChange,
                                password = password,
                                onPasswordChange = onPasswordChange,
                                onSignupClick = onSignupClick,
                                onNavigateToLogin = { isLoginScreen = true }
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
    onNavigateToSignUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        contentAlignment = Alignment.Center
    ){
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
                Text(stringResource(id = R.string.signup))
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
            Button(onClick = onLoginClick) {
                Text(stringResource(id = R.string.login))
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
    onSignupClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)))
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        contentAlignment = Alignment.Center
    ){
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
                Text(stringResource(id = R.string.login))
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
            Button(onClick = onSignupClick) {
                Text(stringResource(id = R.string.signup))
            }
        }
    }

}

@Preview(showBackground = true, widthDp = 720, heightDp = 360)
@Composable
fun PreviewConnectionScreen() {
    NomDuJeuTheme(darkTheme = false, dynamicColor = false) {
        ConnectionScreenContent(
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {}
        )

    }
}
