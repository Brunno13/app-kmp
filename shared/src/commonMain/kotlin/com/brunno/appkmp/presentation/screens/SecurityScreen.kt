package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_revoke_session
import kmpprojectbrunno.shared.generated.resources.action_update_password
import kmpprojectbrunno.shared.generated.resources.desc_biometric_unlock
import kmpprojectbrunno.shared.generated.resources.empty_sessions_desc
import kmpprojectbrunno.shared.generated.resources.empty_sessions_title
import kmpprojectbrunno.shared.generated.resources.label_unknown
import kmpprojectbrunno.shared.generated.resources.label_unknown_device
import kmpprojectbrunno.shared.generated.resources.msg_invalid_current_password
import kmpprojectbrunno.shared.generated.resources.msg_password_updated
import kmpprojectbrunno.shared.generated.resources.placeholder_current_password
import kmpprojectbrunno.shared.generated.resources.placeholder_new_password
import kmpprojectbrunno.shared.generated.resources.subtitle_disable_biometric
import kmpprojectbrunno.shared.generated.resources.subtitle_enable_biometric
import kmpprojectbrunno.shared.generated.resources.title_active_sessions
import kmpprojectbrunno.shared.generated.resources.title_biometric
import kmpprojectbrunno.shared.generated.resources.title_biometric_unlock
import kmpprojectbrunno.shared.generated.resources.title_change_password
import kmpprojectbrunno.shared.generated.resources.title_confirm_action
import kmpprojectbrunno.shared.generated.resources.title_security
import kmpprojectbrunno.shared.generated.resources.warning_biometric
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


private const val USER_AGENT_DISPLAY_MAX_LENGTH = 30
private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3_000L
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
    val sessionError by viewModel.sessionError.collectAsState()
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

            delay(timeMillis = SUCCESS_MESSAGE_DURATION_MILLIS)
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
                    color = MaterialTheme.colorScheme.tertiary,
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

            if (sessionError != null) {
                Text(
                    text = sessionError!!.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
            }

            if (activeSessions.isEmpty()) {
                if (sessionError == null) {
                    MenuCard(
                        title = stringResource(Res.string.empty_sessions_title),
                        subtitle = stringResource(Res.string.empty_sessions_desc),
                        icon = Icons.Default.Info,
                    )
                }
            } else {
                activeSessions.forEach { session ->
                    val unknownDeviceText = stringResource(Res.string.label_unknown_device)
                    val unknownIpText = stringResource(Res.string.label_unknown)

                    MenuCard(
                        title = session.userAgent?.take(USER_AGENT_DISPLAY_MAX_LENGTH) ?: unknownDeviceText,
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
