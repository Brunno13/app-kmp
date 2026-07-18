package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val biometricManager = rememberBiometricManager()
    val titleSecureAccess = stringResource(Res.string.title_secure_access)
    val subtitleSecureAccess = stringResource(Res.string.subtitle_secure_access)

    LaunchedEffect(currentUser) {
        if (currentUser != null && autoLoginState == AutoLoginState.Idle) {
            viewModel.checkAutoLogin(biometricManager.isBiometricAvailable())
        }
    }

    LaunchedEffect(autoLoginState) {
        when (autoLoginState) {
            is AutoLoginState.ProceedToHome -> {
                viewModel.resetAutoLoginState()
                onLoginSuccess()
            }
            is AutoLoginState.RequestBiometrics -> {
                biometricManager.promptBiometricAuth(
                    title = titleSecureAccess,
                    subtitle = subtitleSecureAccess,
                    onSuccess = { viewModel.onBiometricSuccess() },
                    onFailed = { /* Permanece na tela de login para tentar novamente ou digitar senha */ }
                )
            }
            is AutoLoginState.BiometricsRevoked -> {
                // A ViewModel já deslogou o utilizador. Aqui a tela só aguarda.
            }
            is AutoLoginState.Idle -> {}
        }
    }

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
            Text(
                text = stringResource(Res.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(Res.string.placeholder_email)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(Res.string.placeholder_password),
                isPassword = true
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = stringResource(Res.string.action_forgot_password),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = uiState !is LoginUiState.Loading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(MaterialTheme.dimens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is LoginUiState.Loading) {
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

            if (autoLoginState == AutoLoginState.RequestBiometrics) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
                TextButton(onClick = { viewModel.checkAutoLogin(biometricManager.isBiometricAvailable()) }) {
                    Text(
                        text = stringResource(Res.string.action_retry_biometrics),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (autoLoginState == AutoLoginState.BiometricsRevoked) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
                Text(
                    text = stringResource(Res.string.msg_biometrics_revoked),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
                Text(
                    text = (uiState as LoginUiState.Error).error.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.msg_dont_have_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.spaceTiny))
                Text(
                    text = stringResource(Res.string.action_sign_up),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }.padding(MaterialTheme.dimens.spaceTiny)
                )
            }
        }
    }
}