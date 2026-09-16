package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.utils.rememberBiometricManager
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.AutoLoginState
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_forgot_password
import kmpprojectbrunno.shared.generated.resources.action_retry_biometrics
import kmpprojectbrunno.shared.generated.resources.action_sign_in
import kmpprojectbrunno.shared.generated.resources.action_sign_up
import kmpprojectbrunno.shared.generated.resources.msg_biometrics_revoked
import kmpprojectbrunno.shared.generated.resources.msg_dont_have_account
import kmpprojectbrunno.shared.generated.resources.placeholder_email
import kmpprojectbrunno.shared.generated.resources.placeholder_password
import kmpprojectbrunno.shared.generated.resources.subtitle_secure_access
import kmpprojectbrunno.shared.generated.resources.title_secure_access
import kmpprojectbrunno.shared.generated.resources.welcome_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class LoginFormState(
    val email: String = "",
    val password: String = ""
) {
    val isValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}

private class LoginActions(
    val onFormChange: (LoginFormState) -> Unit,
    val onLogin: () -> Unit,
    val onForgotPassword: () -> Unit,
    val onRetryBiometrics: () -> Unit,
    val onRegister: () -> Unit
)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val autoLoginState by viewModel.autoLoginState.collectAsState()
    var form by remember { mutableStateOf(LoginFormState()) }

    val biometricManager = rememberBiometricManager()
    val secureAccessTitle = stringResource(Res.string.title_secure_access)
    val secureAccessSubtitle = stringResource(Res.string.subtitle_secure_access)

    LoginAutoLoginEffects(
        hasCurrentUser = currentUser != null,
        autoLoginState = autoLoginState,
        onCheckAutoLogin = {
            viewModel.checkAutoLogin(biometricManager.isBiometricAvailable())
        },
        onProceedToHome = {
            viewModel.resetAutoLoginState()
            onLoginSuccess()
        },
        onRequestBiometrics = {
            biometricManager.promptBiometricAuth(
                title = secureAccessTitle,
                subtitle = secureAccessSubtitle,
                onSuccess = viewModel::onBiometricSuccess,
                onFailed = {}
            )
        }
    )

    LoginContent(
        form = form,
        uiState = uiState,
        autoLoginState = autoLoginState,
        actions = LoginActions(
            onFormChange = { form = it },
            onLogin = { viewModel.login(form.email, form.password) },
            onForgotPassword = onNavigateToForgotPassword,
            onRetryBiometrics = {
                viewModel.checkAutoLogin(biometricManager.isBiometricAvailable())
            },
            onRegister = onNavigateToRegister
        )
    )
}

@Composable
private fun LoginAutoLoginEffects(
    hasCurrentUser: Boolean,
    autoLoginState: AutoLoginState,
    onCheckAutoLogin: () -> Unit,
    onProceedToHome: () -> Unit,
    onRequestBiometrics: () -> Unit
) {
    LaunchedEffect(hasCurrentUser) {
        if (hasCurrentUser && autoLoginState == AutoLoginState.Idle) {
            onCheckAutoLogin()
        }
    }

    LaunchedEffect(autoLoginState) {
        when (autoLoginState) {
            is AutoLoginState.ProceedToHome -> onProceedToHome()
            is AutoLoginState.RequestBiometrics -> onRequestBiometrics()
            is AutoLoginState.BiometricsRevoked -> Unit
            is AutoLoginState.Idle -> Unit
        }
    }
}

@Composable
private fun LoginContent(
    form: LoginFormState,
    uiState: LoginUiState,
    autoLoginState: AutoLoginState,
    actions: LoginActions
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginHeader()

            LoginCredentials(
                form = form,
                onFormChange = actions.onFormChange,
                onForgotPassword = actions.onForgotPassword
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            LoginSubmitButton(
                isFormValid = form.isValid,
                isLoading = uiState is LoginUiState.Loading,
                onLogin = actions.onLogin
            )

            LoginBiometricStatus(
                autoLoginState = autoLoginState,
                onRetryBiometrics = actions.onRetryBiometrics
            )

            LoginErrorMessage(uiState = uiState)

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            LoginRegisterPrompt(onRegister = actions.onRegister)
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = stringResource(Res.string.welcome_back),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))
}

@Composable
private fun LoginCredentials(
    form: LoginFormState,
    onFormChange: (LoginFormState) -> Unit,
    onForgotPassword: () -> Unit
) {
    AppTextField(
        value = form.email,
        onValueChange = {
            onFormChange(form.copy(email = it))
        },
        placeholder = stringResource(Res.string.placeholder_email)
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    AppTextField(
        value = form.password,
        onValueChange = {
            onFormChange(form.copy(password = it))
        },
        placeholder = stringResource(Res.string.placeholder_password),
        isPassword = true
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        TextButton(onClick = onForgotPassword) {
            Text(
                text = stringResource(Res.string.action_forgot_password),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoginSubmitButton(
    isFormValid: Boolean,
    isLoading: Boolean,
    onLogin: () -> Unit
) {
    Button(
        onClick = onLogin,
        enabled = isFormValid && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(Res.string.action_sign_in),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun LoginBiometricStatus(
    autoLoginState: AutoLoginState,
    onRetryBiometrics: () -> Unit
) {
    when (autoLoginState) {
        is AutoLoginState.RequestBiometrics -> {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            TextButton(onClick = onRetryBiometrics) {
                Text(
                    text = stringResource(Res.string.action_retry_biometrics),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        is AutoLoginState.BiometricsRevoked -> {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            Text(
                text = stringResource(Res.string.msg_biometrics_revoked),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        else -> Unit
    }
}

@Composable
private fun LoginErrorMessage(
    uiState: LoginUiState
) {
    when (val state = uiState) {
        is LoginUiState.Error -> {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            Text(
                text = state.error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        else -> Unit
    }
}

@Composable
private fun LoginRegisterPrompt(
    onRegister: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.msg_dont_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(MaterialTheme.dimens.spaceTiny))

        Text(
            text = stringResource(Res.string.action_sign_up),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onRegister)
                .padding(MaterialTheme.dimens.spaceTiny)
        )
    }
}
