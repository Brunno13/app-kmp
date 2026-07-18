package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.components.AppTopBar
import com.brunno.appkmp.presentation.components.MenuCard
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.utils.rememberBiometricManager
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val biometricManager = rememberBiometricManager()
    val isBiometricAvailable = remember { biometricManager.isBiometricAvailable() }
    val biometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val titleConfirm = stringResource(Res.string.title_confirm_action)
    val subtitleEnable = stringResource(Res.string.subtitle_enable_biometric)
    val subtitleDisable = stringResource(Res.string.subtitle_disable_biometric)

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            currentPassword = ""
            newPassword = ""
            showSuccessMessage = true
            viewModel.resetState()

            delay(3000)
            showSuccessMessage = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = stringResource(Res.string.title_security), onBackClick = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

            Text(
                text = stringResource(Res.string.title_change_password),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            AppTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = stringResource(Res.string.placeholder_current_password),
                isPassword = true
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
            AppTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = stringResource(Res.string.placeholder_new_password),
                isPassword = true
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            if (uiState is LoginUiState.Error) {
                val mappedError = (uiState as LoginUiState.Error).error.asString()
                val isAuthError = mappedError.contains("email", ignoreCase = true)

                Text(
                    text = if (isAuthError) stringResource(Res.string.msg_invalid_current_password) else mappedError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
            }

            Button(
                onClick = { viewModel.changePassword(currentPassword, newPassword) },
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && uiState !is LoginUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(MaterialTheme.dimens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(Res.string.action_update_password), fontWeight = FontWeight.Bold)
                }
            }

            if (showSuccessMessage) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceSmall))
                Text(
                    text = stringResource(Res.string.msg_password_updated),
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            if (isBiometricAvailable) {
                Text(
                    text = stringResource(Res.string.title_biometric),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

                MenuCard(
                    title = stringResource(Res.string.title_biometric_unlock),
                    subtitle = stringResource(Res.string.desc_biometric_unlock),
                    icon = Icons.Default.Lock,
                    trailingContent = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { desiredState ->
                                biometricManager.promptBiometricAuth(
                                    title = titleConfirm,
                                    subtitle = if (desiredState) subtitleEnable else subtitleDisable,
                                    onSuccess = {
                                        viewModel.toggleBiometric(desiredState)
                                    },
                                    onFailed = {
                                        // O estado permanece o mesmo e o Switch volta à posição anterior
                                    }
                                )
                            }
                        )
                    }
                )

                Text(
                    text = stringResource(Res.string.warning_biometric),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = MaterialTheme.dimens.spaceSmall, start = MaterialTheme.dimens.spaceSmall)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))
            }

            Text(
                text = stringResource(Res.string.title_active_sessions),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            if (activeSessions.isEmpty()) {
                MenuCard(
                    title = stringResource(Res.string.empty_sessions_title),
                    subtitle = stringResource(Res.string.empty_sessions_desc),
                    icon = Icons.Default.Info,
                )
            } else {
                activeSessions.forEach { session ->
                    val unknownDeviceText = stringResource(Res.string.label_unknown_device)
                    val unknownIpText = stringResource(Res.string.label_unknown)

                    MenuCard(
                        title = session.userAgent?.take(30) ?: unknownDeviceText,
                        subtitle = "IP: ${session.ipAddress ?: unknownIpText}",
                        icon = Icons.Default.Computer,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    session.token?.let { token ->
                                        viewModel.revokeSession(token) {
                                            onLogoutSuccess()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.action_revoke_session),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceSmall))
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))
        }
    }
}