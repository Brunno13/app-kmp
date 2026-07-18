package com.brunno.appkmp.presentation.utils

import androidx.compose.runtime.Composable

expect class BiometricManager {
    fun isBiometricAvailable(): Boolean
    fun promptBiometricAuth(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    )
}

@Composable
expect fun rememberBiometricManager(): BiometricManager