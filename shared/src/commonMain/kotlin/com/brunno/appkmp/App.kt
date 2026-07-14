package com.brunno.appkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brunno.appkmp.presentation.navigation.Routes
import com.brunno.appkmp.presentation.screens.LoginScreen
import com.brunno.appkmp.presentation.theme.AppTheme
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize().safeContentPadding()
            ) {
                composable(Routes.HOME) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(Res.string.home_title))

                        Button(onClick = { navController.navigate(Routes.LOGIN) }) {
                            Text(stringResource(Res.string.btn_go_to_login))
                        }
                    }
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.DETAILS) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(Res.string.details_title))

                        Button(onClick = { navController.popBackStack() }) {
                            Text(stringResource(Res.string.btn_back))
                        }
                    }
                }
            }
        }
    }
}