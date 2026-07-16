package com.brunno.appkmp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brunno.appkmp.domain.enums.ThemeMode
import com.brunno.appkmp.presentation.components.AppErrorScreen
import com.brunno.appkmp.presentation.components.NetworkBanner
import com.brunno.appkmp.presentation.navigation.Routes
import com.brunno.appkmp.presentation.screens.*
import com.brunno.appkmp.presentation.theme.AppTheme
import com.brunno.appkmp.presentation.utils.GlobalErrorHandler
import com.brunno.appkmp.presentation.utils.NetworkMonitor
import com.brunno.appkmp.presentation.viewmodels.ThemeViewModel
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()

    val globalErrorHandler: GlobalErrorHandler = koinInject()
    val networkMonitor: NetworkMonitor = koinInject()

    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemDark
    }

    val fatalError by globalErrorHandler.fatalError.collectAsState()
    val isOffline by networkMonitor.isOffline.collectAsState(initial = false)

    AppTheme(useDarkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (fatalError != null) {
                AppErrorScreen(
                    onRetry = { globalErrorHandler.clearError() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {

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
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                themeViewModel = themeViewModel
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

                    NetworkBanner(
                        isOffline = isOffline,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}