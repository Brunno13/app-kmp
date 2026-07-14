package com.brunno.appkmp.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val spaceTiny: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 16.dp,
    val spaceLarge: Dp = 24.dp,
    val spaceExtraLarge: Dp = 32.dp,
    val spaceXXL: Dp = 48.dp,
    val spaceHuge: Dp = 56.dp,
    val buttonHeight: Dp = 56.dp,
    val screenPadding: Dp = 24.dp
)

val LocalAppDimens = compositionLocalOf { AppDimens() }