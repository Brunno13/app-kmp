package com.brunno.appkmp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.brunno.appkmp.domain.enums.ThemeMode
import com.brunno.appkmp.presentation.components.AppErrorScreen
import com.brunno.appkmp.presentation.navigation.AppNavHost
import com.brunno.appkmp.presentation.theme.AppTheme
import com.brunno.appkmp.presentation.utils.GlobalErrorHandler
import com.brunno.appkmp.presentation.utils.NetworkMonitor
import com.brunno.appkmp.presentation.viewmodels.ThemeViewModel
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
    val isOffline by networkMonitor.isOffline.collectAsState(
        initial = false
    )

    AppTheme(useDarkTheme = useDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (fatalError != null) {
                AppErrorScreen(
                    onRetry = globalErrorHandler::clearError
                )
            } else {
                AppNavHost(
                    themeViewModel = themeViewModel,
                    isOffline = isOffline
                )
            }
        }
    }
}
