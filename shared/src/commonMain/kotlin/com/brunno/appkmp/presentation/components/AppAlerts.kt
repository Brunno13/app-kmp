package com.brunno.appkmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.brunno.appkmp.presentation.theme.BackgroundDark
import com.brunno.appkmp.presentation.theme.OfflineBannerBgDark
import com.brunno.appkmp.presentation.theme.OfflineBannerBgLight
import com.brunno.appkmp.presentation.theme.OnBackgroundLight
import com.brunno.appkmp.presentation.theme.ToastErrorBgDark
import com.brunno.appkmp.presentation.theme.ToastErrorBgLight
import com.brunno.appkmp.presentation.theme.ToastErrorBorderDark
import com.brunno.appkmp.presentation.theme.ToastErrorBorderLight
import com.brunno.appkmp.presentation.theme.ToastInfoBgDark
import com.brunno.appkmp.presentation.theme.ToastInfoBgLight
import com.brunno.appkmp.presentation.theme.ToastInfoBorderDark
import com.brunno.appkmp.presentation.theme.ToastInfoBorderLight
import com.brunno.appkmp.presentation.theme.ToastSuccessBgDark
import com.brunno.appkmp.presentation.theme.ToastSuccessBgLight
import com.brunno.appkmp.presentation.theme.ToastSuccessBorderDark
import com.brunno.appkmp.presentation.theme.ToastSuccessBorderLight
import com.brunno.appkmp.presentation.theme.dimens
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_ok
import kmpprojectbrunno.shared.generated.resources.action_try_again
import kmpprojectbrunno.shared.generated.resources.error_unexpected_message
import kmpprojectbrunno.shared.generated.resources.error_unexpected_title
import kmpprojectbrunno.shared.generated.resources.network_offline_banner
import org.jetbrains.compose.resources.stringResource

enum class AlertType {
    SUCCESS, ERROR, INFO
}

@Composable
private fun isAppInDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == BackgroundDark
}

@Composable
fun AppToast(
    title: String,
    message: String,
    type: AlertType,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme()
) {
    val (backgroundColor, leftBorderColor, icon) = when (type) {
        AlertType.SUCCESS -> Triple(
            if (isDark) ToastSuccessBgDark else ToastSuccessBgLight,
            if (isDark) ToastSuccessBorderDark else ToastSuccessBorderLight,
            Icons.Rounded.CheckCircle
        )
        AlertType.ERROR -> Triple(
            if (isDark) ToastErrorBgDark else ToastErrorBgLight,
            if (isDark) ToastErrorBorderDark else ToastErrorBorderLight,
            Icons.Rounded.Cancel
        )
        AlertType.INFO -> Triple(
            if (isDark) ToastInfoBgDark else ToastInfoBgLight,
            if (isDark) ToastInfoBorderDark else ToastInfoBorderLight,
            Icons.Rounded.Info
        )
    }

    val contentColor = if (isDark) Color.White else OnBackgroundLight

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.spaceMedium),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 6.dp.toPx()
                    drawRect(
                        color = leftBorderColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(width = strokeWidth, height = size.height)
                    )
                }
                .padding(MaterialTheme.dimens.spaceMedium),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = leftBorderColor,
                modifier = Modifier.size(MaterialTheme.dimens.spaceLarge) // 24.dp
            )

            Spacer(modifier = Modifier.width(MaterialTheme.dimens.spaceSmall))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceTiny))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AppModal(
    title: String,
    message: String,
    buttonText: String = stringResource(Res.string.action_ok),
    type: AlertType,
    isDark: Boolean = isAppInDarkTheme(),
    onDismiss: () -> Unit
) {
    val (primaryColor, icon) = when (type) {
        AlertType.SUCCESS -> Pair(if (isDark) ToastSuccessBorderDark else ToastSuccessBorderLight, Icons.Rounded.CheckCircle)
        AlertType.ERROR -> Pair(if (isDark) ToastErrorBorderDark else ToastErrorBorderLight, Icons.Rounded.Cancel)
        AlertType.INFO -> Pair(if (isDark) ToastInfoBorderDark else ToastInfoBorderLight, Icons.Rounded.Info)
    }

    val textColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.dimens.spaceLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type.name,
                    tint = primaryColor,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceSmall))

                Text(
                    text = message,
                    fontSize = 15.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MaterialTheme.dimens.buttonHeight),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AppErrorScreen(
    title: String = stringResource(Res.string.error_unexpected_title),
    message: String = stringResource(Res.string.error_unexpected_message),
    buttonText: String = stringResource(Res.string.action_try_again),
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(MaterialTheme.dimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.dimens.buttonHeight),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
    }
}

@Composable
fun NetworkBanner(
    isOffline: Boolean,
    text: String = stringResource(Res.string.network_offline_banner),
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme()
) {
    val backgroundColor = if (isDark) OfflineBannerBgDark else OfflineBannerBgLight
    val textColor = if (isDark) Color.Black else Color.White

    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(vertical = MaterialTheme.dimens.spaceMedium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}