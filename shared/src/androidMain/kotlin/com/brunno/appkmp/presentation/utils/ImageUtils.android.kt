package com.brunno.appkmp.presentation.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeBase64ToImageBitmap(base64Str: String): ImageBitmap? {
    return try {
        val bytes = Base64.decode(base64Str)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    } catch (e: Exception) {
        null
    }
}