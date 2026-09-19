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
import androidx.compose.ui.tooling.preview.Preview
import com.brunno.appkmp.domain.model.ActiveSessionInfo
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.components.AppTopBar
import com.brunno.appkmp.presentation.components.MenuCard
import com.brunno.appkmp.presentation.components.MenuCardWithTrailingContent
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.utils.rememberBiometricManager
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.SecurityActionState
import com.brunno.appkmp.presentation.viewmodels.SecurityViewModel
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

private data class SecurityContentState(
    val uiState: SecurityActionState,
    val activeSessions: List<ActiveSessionInfo>,
    val sessionErrorText: String?,
    val biometricEnabled: Boolean,
    val isBiometricAvailable: Boolean
)

private class SecurityActions(
    val onBack: () -> Unit,
    val onChangePassword: (String, String) -> Unit,
    val onResetState: () -> Unit,
    val onToggleBiometric: (Boolean) -> Unit,
    val onRevokeSession: (String) -> Unit
)

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: SecurityViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val sessionError by viewModel.sessionError.collectAsState()
    val biometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    val biometricManager = rememberBiometricManager()
    val isBiometricAvailable = remember {
        biometricManager.isBiometricAvailable()
    }

    val titleConfirm = stringResource(
        Res.string.title_confirm_action
    )
    val subtitleEnable = stringResource(
        Res.string.subtitle_enable_biometric
    )
    val subtitleDisable = stringResource(
        Res.string.subtitle_disable_biometric
    )

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    SecurityContent(
        state = SecurityContentState(
            uiState = uiState,
            activeSessions = activeSessions,
            sessionErrorText = sessionError?.asString(),
            biometricEnabled = biometricEnabled,
            isBiometricAvailable = isBiometricAvailable
        ),
        actions = SecurityActions(
            onBack = onBack,
            onChangePassword = viewModel::changePassword,
            onResetState = viewModel::resetState,
            onToggleBiometric = { desiredState ->
                biometricManager.promptBiometricAuth(
                    title = titleConfirm,
                    subtitle = if (desiredState) {
                        subtitleEnable
                    } else {
                        subtitleDisable
                    },
                    onSuccess = {
                        viewModel.toggleBiometric(desiredState)
                    },
                    onFailed = {
                        // O estado permanece o mesmo.
                    }
                )
            },
            onRevokeSession = { sessionId ->
                viewModel.revokeSession(sessionId) {
                    authViewModel.logout {
                        onLogoutSuccess()
                    }
                }
            }
        )
    )
}

@Composable
private fun SecurityContent(
    state: SecurityContentState,
    actions: SecurityActions
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.title_security),
                onBackClick = actions.onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = MaterialTheme.dimens.screenPadding
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceLarge
                )
            )

            PasswordSection(
                uiState = state.uiState,
                onChangePassword = actions.onChangePassword,
                onResetState = actions.onResetState
            )

            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceExtraLarge
                )
            )

            BiometricSection(
                biometricEnabled = state.biometricEnabled,
                isBiometricAvailable = state.isBiometricAvailable,
                onToggleBiometric = actions.onToggleBiometric
            )

            ActiveSessionsSection(
                activeSessions = state.activeSessions,
                sessionErrorText = state.sessionErrorText,
                onRevokeSession = actions.onRevokeSession
            )

            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceXXL
                )
            )
        }
    }
}

@Composable
private fun PasswordSection(
    uiState: SecurityActionState,
    onChangePassword: (String, String) -> Unit,
    onResetState: () -> Unit
) {
    var currentPassword by remember {
        mutableStateOf("")
    }
    var newPassword by remember {
        mutableStateOf("")
    }
    var showSuccessMessage by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState) {
        if (uiState is SecurityActionState.Success) {
            currentPassword = ""
            newPassword = ""
            showSuccessMessage = true

            onResetState()

            delay(
                timeMillis = SUCCESS_MESSAGE_DURATION_MILLIS
            )

            showSuccessMessage = false
        }
    }

    PasswordFields(
        currentPassword = currentPassword,
        newPassword = newPassword,
        onCurrentPasswordChange = {
            currentPassword = it
        },
        onNewPasswordChange = {
            newPassword = it
        }
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceMedium
        )
    )

    PasswordErrorMessage(
        uiState = uiState
    )

    PasswordSubmitButton(
        currentPassword = currentPassword,
        newPassword = newPassword,
        uiState = uiState,
        onChangePassword = onChangePassword
    )

    PasswordSuccessMessage(
        showSuccessMessage = showSuccessMessage
    )
}

@Composable
private fun PasswordSubmitButton(
    currentPassword: String,
    newPassword: String,
    uiState: SecurityActionState,
    onChangePassword: (String, String) -> Unit
) {
    Button(
        onClick = {
            onChangePassword(
                currentPassword,
                newPassword
            )
        },
        enabled = currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                uiState !is SecurityActionState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(
                MaterialTheme.dimens.buttonHeight
            ),
        shape = MaterialTheme.shapes.medium
    ) {
        if (uiState is SecurityActionState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(
                    MaterialTheme.dimens.spaceLarge
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(
                    Res.string.action_update_password
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PasswordFields(
    currentPassword: String,
    newPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit
) {
    Text(
        text = stringResource(
            Res.string.title_change_password
        ),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceMedium
        )
    )

    AppTextField(
        value = currentPassword,
        onValueChange = onCurrentPasswordChange,
        placeholder = stringResource(
            Res.string.placeholder_current_password
        ),
        isPassword = true
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceMedium
        )
    )

    AppTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        placeholder = stringResource(
            Res.string.placeholder_new_password
        ),
        isPassword = true
    )
}

@Composable
private fun PasswordErrorMessage(
    uiState: SecurityActionState
) {
    if (uiState is SecurityActionState.Error) {
        val mappedError = uiState.error.asString()
        val isAuthError = mappedError.contains(
            other = "email",
            ignoreCase = true
        )

        Text(
            text = if (isAuthError) {
                stringResource(
                    Res.string.msg_invalid_current_password
                )
            } else {
                mappedError
            },
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(
                MaterialTheme.dimens.spaceMedium
            )
        )
    }
}

@Composable
private fun PasswordSuccessMessage(
    showSuccessMessage: Boolean
) {
    if (showSuccessMessage) {
        Spacer(
            modifier = Modifier.height(
                MaterialTheme.dimens.spaceSmall
            )
        )

        Text(
            text = stringResource(
                Res.string.msg_password_updated
            ),
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
    isBiometricAvailable: Boolean,
    onToggleBiometric: (Boolean) -> Unit
) {
    if (!isBiometricAvailable) {
        return
    }

    Text(
        text = stringResource(
            Res.string.title_biometric
        ),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceMedium
        )
    )

    MenuCardWithTrailingContent(
        title = stringResource(
            Res.string.title_biometric_unlock
        ),
        subtitle = stringResource(
            Res.string.desc_biometric_unlock
        ),
        icon = Icons.Default.Lock,
        trailingContent = {
            Switch(
                checked = biometricEnabled,
                onCheckedChange = onToggleBiometric
            )
        }
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceExtraLarge
        )
    )
}

@Composable
private fun ActiveSessionsSection(
    activeSessions: List<ActiveSessionInfo>,
    sessionErrorText: String?,
    onRevokeSession: (String) -> Unit
) {
    Text(
        text = stringResource(
            Res.string.title_active_sessions
        ),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceMedium
        )
    )

    if (sessionErrorText != null) {
        Text(
            text = sessionErrorText,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier = Modifier.height(
                MaterialTheme.dimens.spaceMedium
            )
        )
    }

    if (activeSessions.isEmpty()) {
        if (sessionErrorText == null) {
            MenuCard(
                title = stringResource(
                    Res.string.empty_sessions_title
                ),
                subtitle = stringResource(
                    Res.string.empty_sessions_desc
                ),
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
    session: ActiveSessionInfo,
    onRevokeSession: (String) -> Unit
) {
    val unknownDeviceText = stringResource(
        Res.string.label_unknown_device
    )
    val unknownIpText = stringResource(
        Res.string.label_unknown
    )

    MenuCardWithTrailingContent(
        title = session.userAgent
            ?.take(USER_AGENT_DISPLAY_MAX_LENGTH)
            ?: unknownDeviceText,
        subtitle = "IP: ${
            session.ipAddress ?: unknownIpText
        }",
        icon = Icons.Default.Computer,
        trailingContent = {
            IconButton(
                onClick = {
                    onRevokeSession(session.id)
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
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceSmall
        )
    )
}

@Preview(
    name = "Security",
    showBackground = true
)
@Composable
private fun SecurityScreenPreview() {
    SecurityContent(
        state = SecurityContentState(
            uiState = SecurityActionState.Idle,
            activeSessions = emptyList(),
            sessionErrorText = null,
            biometricEnabled = true,
            isBiometricAvailable = true
        ),
        actions = SecurityActions(
            onBack = {},
            onChangePassword = { _, _ -> },
            onResetState = {},
            onToggleBiometric = {},
            onRevokeSession = {}
        )
    )
}
