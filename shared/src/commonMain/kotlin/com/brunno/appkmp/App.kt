package com.brunno.appkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.brunno.appkmp.presentation.screens.EditProfileScreen
import com.brunno.appkmp.presentation.screens.ForgotPasswordScreen
import com.brunno.appkmp.presentation.screens.HomeScreen
import com.brunno.appkmp.presentation.screens.LoginScreen
import com.brunno.appkmp.presentation.screens.ProfileScreen
import com.brunno.appkmp.presentation.screens.RegisterScreen
import com.brunno.appkmp.presentation.screens.SecurityScreen
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
                startDestination = Routes.LOGIN,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Routes.FORGOT_PASSWORD)
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onNavigateToLogin = {
                            navController.popBackStack()
                        },
                        onRegisterSuccess = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                composable(Routes.FORGOT_PASSWORD) {
                    ForgotPasswordScreen(
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        onNavigateToHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onNavigateToHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Routes.EDIT_PROFILE)
                        },
                        onNavigateToSecurity = {
                            navController.navigate(Routes.SECURITY)
                        },
                        onLogoutSuccess = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.SECURITY) {
                    SecurityScreen(
                        onBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
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