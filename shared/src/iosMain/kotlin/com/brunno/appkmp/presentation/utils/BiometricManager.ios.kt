package com.brunno.appkmp.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.LocalAuthentication.*
import platform.Foundation.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual class BiometricManager {
    actual fun isBiometricAvailable(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    actual fun promptBiometricAuth(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val context = LAContext()
        context.evaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, title) { success, error ->
            dispatch_async(dispatch_get_main_queue()) {
                if (success) {
                    onSuccess()
                } else {
                    onFailed(error?.localizedDescription ?: "Falha na biometria")
                }
            }
        }
    }
}

@Composable
actual fun rememberBiometricManager(): BiometricManager {
    return remember { BiometricManager() }
}