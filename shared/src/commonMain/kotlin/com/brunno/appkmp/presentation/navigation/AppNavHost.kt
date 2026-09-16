package com.brunno.appkmp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brunno.appkmp.presentation.components.NetworkBanner
import com.brunno.appkmp.presentation.screens.DetailsScreen
import com.brunno.appkmp.presentation.screens.EditProfileScreen
import com.brunno.appkmp.presentation.screens.ForgotPasswordScreen
import com.brunno.appkmp.presentation.screens.HomeScreen
import com.brunno.appkmp.presentation.screens.LoginScreen
import com.brunno.appkmp.presentation.screens.ProfileScreen
import com.brunno.appkmp.presentation.screens.RegisterScreen
import com.brunno.appkmp.presentation.screens.SecurityScreen
import com.brunno.appkmp.presentation.viewmodels.ThemeViewModel

@Composable
fun AppNavHost(
    themeViewModel: ThemeViewModel,
    isOffline: Boolean
) {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.fillMaxSize()
        ) {
            authRoutes(navController)
            homeRoute(navController)
            profileRoutes(
                navController = navController,
                themeViewModel = themeViewModel
            )
            detailsRoute(navController)
        }

        NetworkBanner(
            isOffline = isOffline,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private fun NavGraphBuilder.authRoutes(
    navController: NavHostController
) {
    composable(Routes.LOGIN) {
        LoginScreen(
            onLoginSuccess = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) {
                        inclusive = true
                    }
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
}

private fun NavGraphBuilder.homeRoute(
    navController: NavHostController
) {
    composable(Routes.HOME) {
        HomeScreen(
            onNavigateToHome = {
                navController.navigateTopLevel(Routes.HOME)
            },
            onNavigateToProfile = {
                navController.navigateTopLevel(Routes.PROFILE)
            }
        )
    }
}

private fun NavGraphBuilder.profileRoutes(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    composable(Routes.PROFILE) {
        ProfileScreen(
            navigation = ProfileNavigationActions(
                onNavigateToHome = {
                    navController.navigateTopLevel(Routes.HOME)
                },
                onNavigateToProfile = {
                    navController.navigateTopLevel(Routes.PROFILE)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Routes.EDIT_PROFILE)
                },
                onNavigateToSecurity = {
                    navController.navigate(Routes.SECURITY)
                }
            ),
            onLogoutSuccess = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) {inclusive = true}
                }
            },
            themeViewModel = themeViewModel
        )
    }
    composable(Routes.EDIT_PROFILE) {
        EditProfileScreen(
            onBack = {
                navController.popBackStack()
            }
        )
    }
    composable(Routes.SECURITY) {
        SecurityScreen(
            onBack = {
                navController.popBackStack()
            },
            onLogoutSuccess = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) {inclusive = true}
                }
            }
        )
    }
}

private fun NavGraphBuilder.detailsRoute(
    navController: NavHostController
) {
    composable(Routes.DETAILS) {
        DetailsScreen(
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

private fun NavHostController.navigateTopLevel(
    route: String
) {
    navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
