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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import fr.eseo.ld.android.cp.nomdujeu.ui.navigation.NomDuJeuScreens
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.AuthenticationViewModel
import fr.eseo.ld.android.cp.nomdujeu.viewmodels.GameViewModel

@Composable
fun ConnectionScreen(navController: NavController, authenticationViewModel: AuthenticationViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


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
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { "Email" }
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { "Password" },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(onClick = {
                        authenticationViewModel.loginWithEmail(email, password)
                        navController.popBackStack()
                    }) {
                        Text(text = "Login")
                    }
                    Button(onClick = {
                        authenticationViewModel.signupWithEmail(email, password, pseudo = "ee")
                        navController.popBackStack()
                    }) {
                        Text(text = "Sign up")
                    }
                }
            }
        )
    }
}





@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectionScreenPreview() {
    val navController: NavHostController = rememberNavController()
    val authenticationViewModel : AuthenticationViewModel = hiltViewModel()

    ConnectionScreen(navController, authenticationViewModel )
}