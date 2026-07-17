package com.brunno.appkmp.presentation.utils

import androidx.compose.runtime.Composable

class CameraLauncher(val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}

@Composable
expect fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher