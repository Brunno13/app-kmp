package com.brunno.appkmp.presentation.utils

import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

actual class BiometricManager(private val activity: FragmentActivity) {

    actual fun isBiometricAvailable(): Boolean {
        val biometricManager = AndroidBiometricManager.from(activity)
        return biometricManager.canAuthenticate(AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG) == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    actual fun promptBiometricAuth(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailed(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    onFailed("Biometria não reconhecida.")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
actual fun rememberBiometricManager(): BiometricManager {
    val context = LocalContext.current as FragmentActivity
    return remember { BiometricManager(context) }
}