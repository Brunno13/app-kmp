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
import com.brunno.appkmp.data.remote.models.ActiveSession
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
import com.brunno.appkmp.presentation.components.MenuCardWithTrailingContent

private const val USER_AGENT_DISPLAY_MAX_LENGTH = 30
private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3_000L

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val sessionError by viewModel.sessionError.collectAsState()
    val biometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.title_security),
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceLarge))

            PasswordSection(
                uiState = uiState,
                onChangePassword = viewModel::changePassword,
                onResetState = viewModel::resetState
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            BiometricSection(
                biometricEnabled = biometricEnabled,
                onToggleBiometric = viewModel::toggleBiometric
            )

            ActiveSessionsSection(
                activeSessions = activeSessions,
                sessionErrorText = sessionError?.asString(),
                onRevokeSession = { token ->
                    viewModel.revokeSession(token) {
                        onLogoutSuccess()
                    }
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))
        }
    }
}

@Composable
private fun PasswordSection(
    uiState: LoginUiState,
    onChangePassword: (String, String) -> Unit,
    onResetState: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            currentPassword = ""
            newPassword = ""
            showSuccessMessage = true
            onResetState()

            delay(timeMillis = SUCCESS_MESSAGE_DURATION_MILLIS)
            showSuccessMessage = false
        }
    }

    PasswordFields(
        currentPassword = currentPassword,
        newPassword = newPassword,
        onCurrentPasswordChange = { currentPassword = it },
        onNewPasswordChange = { newPassword = it }
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    PasswordErrorMessage(uiState = uiState)

    Button(
        onClick = {
            onChangePassword(currentPassword, newPassword)
        },
        enabled = currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                uiState !is LoginUiState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium
    ) {
        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(Res.string.action_update_password),
                fontWeight = FontWeight.Bold
            )
        }
    }

    PasswordSuccessMessage(showSuccessMessage = showSuccessMessage)
}

@Composable
private fun PasswordFields(
    currentPassword: String,
    newPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit
) {
    Text(
        text = stringResource(Res.string.title_change_password),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    AppTextField(
        value = currentPassword,
        onValueChange = onCurrentPasswordChange,
        placeholder = stringResource(Res.string.placeholder_current_password),
        isPassword = true
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    AppTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        placeholder = stringResource(Res.string.placeholder_new_password),
        isPassword = true
    )
}

@Composable
private fun PasswordErrorMessage(
    uiState: LoginUiState
) {
    if (uiState is LoginUiState.Error) {
        val mappedError = uiState.error.asString()
        val isAuthError = mappedError.contains("email", ignoreCase = true)

        Text(
            text = if (isAuthError) {
                stringResource(Res.string.msg_invalid_current_password)
            } else {
                mappedError
            },
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
    }
}

@Composable
private fun PasswordSuccessMessage(
    showSuccessMessage: Boolean
) {
    if (showSuccessMessage) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceSmall))

        Text(
            text = stringResource(Res.string.msg_password_updated),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BiometricSection(
    biometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit
) {
    val biometricManager = rememberBiometricManager()
    val isBiometricAvailable = remember {
        biometricManager.isBiometricAvailable()
    }

    if (isBiometricAvailable) {
        val titleConfirm = stringResource(Res.string.title_confirm_action)
        val subtitleEnable = stringResource(Res.string.subtitle_enable_biometric)
        val subtitleDisable = stringResource(Res.string.subtitle_disable_biometric)

        Text(
            text = stringResource(Res.string.title_biometric),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

        MenuCardWithTrailingContent(
            title = stringResource(Res.string.title_biometric_unlock),
            subtitle = stringResource(Res.string.desc_biometric_unlock),
            icon = Icons.Default.Lock,
            trailingContent = {
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { desiredState ->
                        biometricManager.promptBiometricAuth(
                            title = titleConfirm,
                            subtitle = if (desiredState) {
                                subtitleEnable
                            } else {
                                subtitleDisable
                            },
                            onSuccess = {
                                onToggleBiometric(desiredState)
                            },
                            onFailed = {
                                // O estado permanece o mesmo.
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
            modifier = Modifier.padding(
                top = MaterialTheme.dimens.spaceSmall,
                start = MaterialTheme.dimens.spaceSmall
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))
    }
}

@Composable
private fun ActiveSessionsSection(
    activeSessions: List<ActiveSession>,
    sessionErrorText: String?,
    onRevokeSession: (String) -> Unit
) {
    Text(
        text = stringResource(Res.string.title_active_sessions),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    if (sessionErrorText != null) {
        Text(
            text = sessionErrorText,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
    }

    if (activeSessions.isEmpty()) {
        if (sessionErrorText == null) {
            MenuCard(
                title = stringResource(Res.string.empty_sessions_title),
                subtitle = stringResource(Res.string.empty_sessions_desc),
                icon = Icons.Default.Info
            )
        }
    } else {
        activeSessions.forEach { session ->
            ActiveSessionCard(
                session = session,
                onRevokeSession = onRevokeSession
            )
        }
    }
}

@Composable
private fun ActiveSessionCard(
    session: ActiveSession,
    onRevokeSession: (String) -> Unit
) {
    val unknownDeviceText = stringResource(Res.string.label_unknown_device)
    val unknownIpText = stringResource(Res.string.label_unknown)

    MenuCardWithTrailingContent(
        title = session.userAgent
            ?.take(USER_AGENT_DISPLAY_MAX_LENGTH)
            ?: unknownDeviceText,
        subtitle = "IP: ${session.ipAddress ?: unknownIpText}",
        icon = Icons.Default.Computer,
        trailingContent = {
            IconButton(
                onClick = {
                    session.token?.let { token ->
                        onRevokeSession(token)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        Res.string.action_revoke_session
                    ),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )

    Spacer(
        modifier = Modifier.height(MaterialTheme.dimens.spaceSmall)
    )
}
