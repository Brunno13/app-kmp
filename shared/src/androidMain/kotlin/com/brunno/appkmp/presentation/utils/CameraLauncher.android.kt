package com.brunno.appkmp.presentation.utils

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.ByteArrayOutputStream

private const val JPEG_COMPRESSION_QUALITY = 90

@Composable
actual fun rememberCameraLauncher(
    onResult: (ByteArray?) -> Unit
): CameraLauncher {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                JPEG_COMPRESSION_QUALITY,
                stream
            )

            onResult(stream.toByteArray())
        } else {
            onResult(null)
        }
    }

    return remember {
        CameraLauncher {
            launcher.launch(null)
        }
    }
}
